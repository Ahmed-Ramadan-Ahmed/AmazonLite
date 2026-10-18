package com.amazonlite.inventory.listener;

import com.amazonlite.common.events.OrderCreatedEvent;
import com.amazonlite.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order-created-events", groupId = "inventory-saga-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for Order ID: {}", event.getOrderId());
        inventoryService.processOrderInventory(event);
    }
}