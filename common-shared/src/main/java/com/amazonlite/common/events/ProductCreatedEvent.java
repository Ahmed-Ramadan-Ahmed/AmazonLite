package com.amazonlite.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {

    private String productId;
    private String sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private String sku;
    private boolean active;
}