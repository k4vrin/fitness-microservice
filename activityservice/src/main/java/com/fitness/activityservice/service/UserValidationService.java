package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean isValidUser(String userId) {
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
                .blockOptional()
                .orElse(false);
    }
}
