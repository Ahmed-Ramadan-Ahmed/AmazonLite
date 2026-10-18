package com.amazonlite.inventory.service;

import com.amazonlite.common.events.InventoryResponseEvent;
import com.amazonlite.common.events.OrderCreatedEvent;
import com.amazonlite.inventory.entity.Inventory;
import com.amazonlite.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String SAGA_REPLY_TOPIC = "saga-reply-events";

    @Transactional
    public void processOrderInventory(OrderCreatedEvent event) {
        log.info("Processing inventory reservation for Order ID: {}", event.getOrderId());

        boolean allReserved = true;
        String failureReason = "";

        // 1. Check & Reserve Stock for all line items
        for (Map.Entry<String, Integer> item : event.getItems().entrySet()) {
            String sku = item.getKey();
            Integer quantity = item.getValue();

            Optional<Inventory> inventoryOptional = inventoryRepository.findBySku(sku);

            if (inventoryOptional.isEmpty()) {
                allReserved = false;
                failureReason = "SKU not found in inventory: " + sku;
                break;
            }

            Inventory inventory = inventoryOptional.get();

            if (inventory.getAvailableQuantity() < quantity) {
                allReserved = false;
                failureReason = "Insufficient stock for SKU: " + sku + ". Requested: " + quantity + ", Available: " + inventory.getAvailableQuantity();
                break;
            }

            // Lock quantities
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
            inventoryRepository.save(inventory);
        }

        // 2. Publish Reply to Saga Orchestrator
        InventoryResponseEvent responseEvent = InventoryResponseEvent.builder()
                .orderId(event.getOrderId())
                .success(allReserved)
                .message(allReserved ? "Inventory reserved successfully" : failureReason)
                .build();

        log.info("Sending Saga response for Order {}: Success={}", event.getOrderId(), allReserved);
        kafkaTemplate.send(SAGA_REPLY_TOPIC, event.getOrderId(), responseEvent);
    }
}