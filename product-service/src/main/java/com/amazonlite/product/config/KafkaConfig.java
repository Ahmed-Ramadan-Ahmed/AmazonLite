package com.amazonlite.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String PRODUCT_CREATED_TOPIC = "product-created-events";

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(PRODUCT_CREATED_TOPIC)
                .partitions(3) // 3 partitions allow us to scale out consumers later
                .replicas(1)
                .build();
    }
}