package com.amazonlite.auth.repository;

import com.amazonlite.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    // Spring Data Redis will automatically implement this thanks to the @Indexed annotation on email
    Iterable<RefreshToken> findByEmail(String email);
}