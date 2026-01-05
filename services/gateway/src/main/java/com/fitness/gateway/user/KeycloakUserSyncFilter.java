package com.fitness.gateway.user;

import com.fitness.gateway.dto.RegisterRequest;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {

    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (userId != null && token != null && token.startsWith("Bearer ")) {
            return userService
                    .isValidUser(userId)
                    .flatMap(exist -> {
                        if (exist) {
                            log.info("User with id {} already exist", userId);
                            return Mono.empty();
                        } else {
                            log.info("Syncing user with id: {}", userId);
                            RegisterRequest registerRequest = getUserDetailsFromToken(token);
                            if (registerRequest != null) {
                                return userService.register(registerRequest);
                            } else {
                                return Mono.empty();
                            }

                        }
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .header("X-User-Id", userId).build();
                        return chain.filter(exchange.mutate().request(request).build());
                    }));
        }

        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetailsFromToken(String token) {
        try {
            String tokenWithoutBearer = token.replace("Bearer ", "");
            SignedJWT jwt = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setKeycloakId(claimsSet.getSubject());
            registerRequest.setEmail(claimsSet.getStringClaim("email"));
            registerRequest.setPassword("dummyPassword123!"); // dummy password, not really used
            registerRequest.setFirstName(claimsSet.getStringClaim("given_name"));
            registerRequest.setLastName(claimsSet.getStringClaim("family_name"));
            return registerRequest;
        } catch (Exception e) {
            log.error("Error while parsing JWT Token", e);
            return null;
        }
    }
}
