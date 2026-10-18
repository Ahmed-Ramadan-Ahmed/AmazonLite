package com.amazonlite.order.listener;

import com.amazonlite.common.events.InventoryResponseEvent;
import com.amazonlite.order.config.KafkaConfig;
import com.amazonlite.order.service.OrderOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {

    private final OrderOrchestratorService orderOrchestratorService;

    @KafkaListener(topics = KafkaConfig.SAGA_REPLY_TOPIC, groupId = "order-saga-group")
    public void consumeInventoryResponse(InventoryResponseEvent event) {
        log.info("Received Saga reply for Order ID {}: Success={}, Message={}",
                event.getOrderId(), event.isSuccess(), event.getMessage());

        orderOrchestratorService.handleInventoryResponse(event.getOrderId(), event.isSuccess(), event.getMessage());
    }
}