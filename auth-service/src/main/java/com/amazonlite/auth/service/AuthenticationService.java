package com.amazonlite.auth.service;

import com.amazonlite.auth.dto.RegisterRequest;
import com.amazonlite.auth.entity.Customer;
import com.amazonlite.auth.entity.Role;
import com.amazonlite.auth.entity.Seller;
import com.amazonlite.auth.entity.SellerStatus;
import com.amazonlite.auth.repository.CustomerRepository;
import com.amazonlite.auth.repository.SellerRepository;
import com.amazonlite.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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