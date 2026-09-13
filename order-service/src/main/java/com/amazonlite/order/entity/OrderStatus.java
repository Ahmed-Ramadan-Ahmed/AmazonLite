package com.amazonlite.order.entity;

public enum OrderStatus {
    PENDING,            // 1. Order created, waiting for inventory deduction
    INVENTORY_RESERVED, // 2. Inventory confirmed, waiting for payment
    COMPLETED,          // 3. Payment successful, order is ready for shipping
    CANCELLED           // 4. Failed at inventory or payment, saga rolled back
}