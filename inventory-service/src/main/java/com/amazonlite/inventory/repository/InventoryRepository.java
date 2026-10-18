package com.amazonlite.inventory.repository;

import com.amazonlite.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    // Pessimistic Write Lock prevents two concurrent orders from reading the same available quantity
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findBySku(String sku);
}