package com.amazonlite.product.command.repository;

import com.amazonlite.product.command.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // Ensure sellers don't reuse the same SKU
    boolean existsBySku(String sku);
}