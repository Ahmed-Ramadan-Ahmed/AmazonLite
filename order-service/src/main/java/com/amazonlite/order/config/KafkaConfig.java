package com.amazonlite.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created-events";
    public static final String SAGA_REPLY_TOPIC = "saga-reply-events";

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic sagaReplyTopic() {
        return TopicBuilder.name(SAGA_REPLY_TOPIC).partitions(3).replicas(1).build();
    }
}