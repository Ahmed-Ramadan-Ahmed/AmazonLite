package com.amazonlite.order.controller;

import com.amazonlite.order.dto.CreateOrderRequest;
import com.amazonlite.order.service.OrderOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderOrchestratorService orderOrchestratorService;

    @PostMapping
    public ResponseEntity<String> placeOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-Auth-User-Id") String customerId,
            @RequestHeader("X-Auth-Role") String role) {

        if (!"ROLE_CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only customers can place orders.");
        }

        String orderId = orderOrchestratorService.createPendingOrder(request, customerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Order received and is pending inventory validation. ID: " + orderId);
    }
}