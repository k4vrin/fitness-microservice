package com.fitness.gateway.user;

import com.fitness.gateway.dto.RegisterRequest;
import com.fitness.gateway.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient userServiceWebClient;

    public Mono<UserResponse> register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());
        return userServiceWebClient
                .post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(new RuntimeException("User service not found"))
                )
                .onStatus(
                        status -> status == HttpStatus.BAD_REQUEST,
                        response -> Mono.error(new RuntimeException("Invalid user registration data"))
                )
                .onStatus(
                        status -> status == HttpStatus.INTERNAL_SERVER_ERROR,
                        response -> Mono.error(new RuntimeException("User service internal error"))
                )
                .bodyToMono(UserResponse.class);
    }

    public Mono<Boolean> isValidUser(String userId) {
        return userServiceWebClient
                .get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(new RuntimeException("User not found with id: " + userId))
                )
                .onStatus(
                        status -> status == HttpStatus.BAD_REQUEST,
                        response -> Mono.error(new RuntimeException("Invalid user id " + userId))
                )
                .bodyToMono(Boolean.class)
                .onErrorResume(e -> {
                    log.error("Error validating user with id {}: {}", userId, e.getMessage());
                    return Mono.just(false);
                });
    }
}
