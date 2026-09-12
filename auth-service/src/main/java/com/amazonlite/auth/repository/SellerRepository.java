package com.amazonlite.auth.repository;

import com.amazonlite.auth.entity.Seller;
import com.amazonlite.auth.entity.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SellerRepository extends JpaRepository<Seller, UUID> {

    // For Admins to fetch a list of sellers waiting for approval
    List<Seller> findByStatus(SellerStatus status);

    Optional<Seller> findByStoreName(String storeName);

    boolean existsByStoreName(String storeName);
}