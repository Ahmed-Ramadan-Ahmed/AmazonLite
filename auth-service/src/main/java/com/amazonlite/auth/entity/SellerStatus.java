package com.amazonlite.auth.entity;

public enum SellerStatus {
    PENDING_APPROVAL,   // Seller registered, waiting for Admin
    APPROVED,           // Admin approved, can list products
    REJECTED            // Admin denied request
}