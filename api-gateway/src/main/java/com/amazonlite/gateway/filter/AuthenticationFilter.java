package com.amazonlite.gateway.filter;

import com.amazonlite.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1. Check if the route requires authentication
        if (validator.isSecured.test(exchange.getRequest())) {

            // 2. Ensure Authorization header exists
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete(); // Stop request and return 401
            }

            // 3. Extract the Bearer token
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7); // Remove "Bearer " prefix
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // 4. Validate and Propagate
            try {
                jwtUtil.validateToken(authHeader);
                Claims claims = jwtUtil.extractAllClaims(authHeader);

                // Read our custom claims from the token
                String userId = claims.get("userId", String.class);
                String role = claims.get("role", String.class);
                String email = claims.getSubject();

                // Mutate the request to add our clean backend headers
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User-Id", userId)
                        .header("X-Auth-Email", email)
                        .header("X-Auth-Role", role)
                        .build();

                // Replace the original request with the mutated one
                exchange = exchange.mutate().request(mutatedRequest).build();

            } catch (Exception e) {
                // Token is expired or invalid
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // 5. Forward the request to the downstream microservice
        return chain.filter(exchange);
    }

    // Give this filter high priority so it runs before routing takes place
    @Override
    public int getOrder() {
        return -1;
    }
}