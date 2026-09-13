package com.amazonlite.product.command.controller;

import com.amazonlite.product.command.dto.CreateProductRequest;
import com.amazonlite.product.command.service.ProductCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {

    private final ProductCommandService productCommandService;

    @PostMapping
    public ResponseEntity<String> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader("X-Auth-User-Id") String sellerId,
            @RequestHeader("X-Auth-Role") String role) {

        // Extra layer of security to ensure only sellers can create products
        if (!"ROLE_SELLER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only authorized sellers can create products.");
        }

        String productId = productCommandService.createProduct(request, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product created with ID: " + productId);
    }
}