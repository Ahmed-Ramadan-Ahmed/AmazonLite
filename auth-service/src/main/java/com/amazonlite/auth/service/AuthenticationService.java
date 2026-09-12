package com.amazonlite.auth.service;

import com.amazonlite.auth.dto.*;
import com.amazonlite.auth.entity.Customer;
import com.amazonlite.auth.entity.Role;
import com.amazonlite.auth.entity.Seller;
import com.amazonlite.auth.entity.SellerStatus;
import com.amazonlite.auth.repository.CustomerRepository;
import com.amazonlite.auth.repository.SellerRepository;
import com.amazonlite.auth.repository.UserRepository;
import com.amazonlite.auth.entity.RefreshToken;
import com.amazonlite.auth.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshExpiration;



    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        if (request.getRole() == Role.ROLE_CUSTOMER) {
            registerCustomer(request);
            return "Customer registered successfully.";
        } else if (request.getRole() == Role.ROLE_SELLER) {
            registerSeller(request);
            // Notice we enforce the business rule we agreed upon earlier:
            return "Seller registered successfully. Waiting for Admin approval.";
        } else {
            throw new IllegalArgumentException("Invalid role for registration.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        // This will automatically verify the password and throw an exception if it's wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach this line, the user is fully authenticated
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Save Refresh Token to Redis
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .email(user.getEmail())
                .expiration(refreshExpiration / 1000) // Spring Data Redis expects TTL in seconds
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        // 1. Verify token exists in Redis (hasn't been revoked or expired)
        refreshTokenRepository.findById(requestToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is missing or expired!"));

        // 2. Extract email and find user
        String email = jwtService.extractUsername(requestToken);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. Verify JWT signature and expiration
        if (!jwtService.isTokenValid(requestToken, user)) {
            throw new IllegalArgumentException("Invalid refresh token signature");
        }

        // 4. Issue a new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(requestToken) // We return the same refresh token
                .role(user.getRole().name())
                .build();
    }

    private void registerCustomer(RegisterRequest request) {
        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ROLE_CUSTOMER)
                .phoneNumber(request.getPhoneNumber())
                .build();

        customerRepository.save(customer);
    }

    private void registerSeller(RegisterRequest request) {
        if (request.getStoreName() == null || request.getStoreName().isBlank()) {
            throw new IllegalArgumentException("Store name is required for sellers.");
        }
//        if (sellerRepository.existsByStoreName(request.getStoreName())) {
//            throw new IllegalArgumentException("Store name is already taken.");
//        }

        Seller seller = Seller.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ROLE_SELLER)
                .status(SellerStatus.PENDING_APPROVAL) // Sellers start as Pending
                .storeName(request.getStoreName())
                .build();

        sellerRepository.save(seller);
    }

}