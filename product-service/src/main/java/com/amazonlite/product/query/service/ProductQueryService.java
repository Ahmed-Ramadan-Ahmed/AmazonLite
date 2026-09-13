package com.amazonlite.product.query.service;

import com.amazonlite.common.events.ProductCreatedEvent;
import com.amazonlite.product.config.KafkaConfig;
import com.amazonlite.product.query.document.ProductDocument;
import com.amazonlite.product.query.repository.ProductDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQueryService {

    private final ProductDocumentRepository productDocumentRepository;

    @KafkaListener(topics = KafkaConfig.PRODUCT_CREATED_TOPIC, groupId = "product-cqrs-group")
    public void consumeProductCreatedEvent(ProductCreatedEvent event) {
        log.info("Received ProductCreatedEvent for SKU: {}", event.getSku());

        ProductDocument productDocument = ProductDocument.builder()
                .id(event.getProductId())
                .sellerId(event.getSellerId())
                .title(event.getTitle())
                .description(event.getDescription())
                .price(event.getPrice())
                .sku(event.getSku())
                .active(event.isActive())
                .tags(new ArrayList<>()) // We can populate this later using AI or seller input
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productDocumentRepository.save(productDocument);
        log.info("Successfully synced product {} to MongoDB read model.", event.getProductId());
    }
}