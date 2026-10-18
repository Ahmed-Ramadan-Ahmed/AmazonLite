package com.amazonlite.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseEvent {
    private String orderId;
    private boolean success;
    private String message; // E.g., "Out of stock for SKU: 123"
}