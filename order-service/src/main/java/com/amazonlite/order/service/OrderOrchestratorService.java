package com.amazonlite.order.service;

import java.util.UUID;
import com.amazonlite.common.events.OrderCreatedEvent;
import com.amazonlite.order.config.KafkaConfig;
import com.amazonlite.order.dto.CreateOrderRequest;
import com.amazonlite.order.entity.Order;
import com.amazonlite.order.entity.OrderLineItem;
import com.amazonlite.order.entity.OrderStatus;
import com.amazonlite.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestratorService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public String createPendingOrder(CreateOrderRequest request, String customerId) {

        // 1. Map DTO to Entities
        List<OrderLineItem> lineItems = request.getItems().entrySet().stream()
                .map(entry -> OrderLineItem.builder()
                        .sku(entry.getKey())
                        .quantity(entry.getValue())
                        .price(BigDecimal.ZERO) // In a full implementation, we'd fetch the live price from Product Service via a synchronous Feign Client call or cached view
                        .build())
                .collect(Collectors.toList());

        // 2. Save Order in PENDING State
        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING) // Strict Saga State
                .totalAmount(BigDecimal.ZERO)
                .lineItems(lineItems)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} created in PENDING state. Starting Saga Orchestration...", savedOrder.getId());

        // 3. Fire Event to trigger Inventory Service
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId().toString())
                .customerId(customerId)
                .totalAmount(savedOrder.getTotalAmount())
                .items(request.getItems())
                .build();

        kafkaTemplate.send(KafkaConfig.ORDER_CREATED_TOPIC, event.getOrderId(), event);

        return savedOrder.getId().toString();
    }

    @Transactional
    public void handleInventoryResponse(String orderId, boolean success, String message) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (success) {
            order.setStatus(OrderStatus.INVENTORY_RESERVED);
            log.info("Saga Step 1 Successful: Order {} inventory reserved. Next step is Payment.", orderId);

            // In Phase 4, we will fire a PaymentRequestedEvent from here.
        } else {
            order.setStatus(OrderStatus.CANCELLED);
            log.warn("Saga Step 1 Failed: Order {} cancelled. Reason: {}", orderId, message);

            // If we had taken money first, this is where we would fire a RefundEvent (Compensating Transaction).
        }

        orderRepository.save(order);
    }
}