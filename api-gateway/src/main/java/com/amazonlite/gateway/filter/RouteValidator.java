package com.amazonlite.gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/eureka",
            "/v3/api-docs",
            "/swagger-ui"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Check if the path is in the explicitly open list (Auth, Eureka, Swagger)
        boolean isExplicitlyOpen = openApiEndpoints.stream().anyMatch(path::contains);
        if (isExplicitlyOpen) {
            return false; // Not secured
        }

        // 2. Allow public READ access to the product catalog
        // But enforce security if the seller is trying to view their private inventory
        if (path.contains("/api/v1/products")
                && method != null
                && method.equals(HttpMethod.GET)
                && !path.contains("/my-inventory")) {
            return false; // Not secured
        }

        // 3. Everything else (POST, PUT, DELETE, or any other path) requires a JWT
        return true;
    };
}