package com.amazonlite.auth.repository;

import com.amazonlite.auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    // Standard CRUD operations inherited. Add customer-specific queries here later if needed.
}