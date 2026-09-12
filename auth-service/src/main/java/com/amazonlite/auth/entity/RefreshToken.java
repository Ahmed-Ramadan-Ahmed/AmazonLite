package com.amazonlite.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RefreshToken") // This tells Spring to store this in Redis, not PostgreSQL
public class RefreshToken {

    @Id
    private String token; // The actual JWT string acts as the unique Redis ID (Key)

    @Indexed // Allows us to search for all tokens belonging to a specific email if we want to "logout from all devices"
    private String email;

    @TimeToLive
    private Long expiration; // Redis will automatically delete the record when this TTL (in seconds) reaches zero
}