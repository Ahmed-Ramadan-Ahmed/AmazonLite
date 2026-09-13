package com.amazonlite.product.command.service;

import com.amazonlite.common.events.ProductCreatedEvent;
import com.amazonlite.product.command.dto.CreateProductRequest;
import com.amazonlite.product.command.entity.Product;
import com.amazonlite.product.command.repository.ProductRepository;
import com.amazonlite.product.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public String createProduct(CreateProductRequest request, String sellerId) {

        // 1. Domain Validation
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists.");
        }

        // 2. Save to PostgreSQL (The Source of Truth)
        Product product = Product.builder()
                .sellerId(sellerId)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .sku(request.getSku())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);

        // 3. Publish Event to Kafka for the Query Service (MongoDB) to consume
        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .productId(savedProduct.getId().toString())
                .sellerId(savedProduct.getSellerId())
                .title(savedProduct.getTitle())
                .description(savedProduct.getDescription())
                .price(savedProduct.getPrice())
                .sku(savedProduct.getSku())
                .active(savedProduct.isActive())
                .build();

        // We use the product ID as the Kafka key to ensure events for the same product go to the same partition
        kafkaTemplate.send(KafkaConfig.PRODUCT_CREATED_TOPIC, event.getProductId(), event);

        return savedProduct.getId().toString();
    }
}