package com.amazonlite.order.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    // A map of SKU strings to the quantity the customer wants to buy
    @NotEmpty(message = "Order must contain at least one item")
    private Map<String, Integer> items;
}