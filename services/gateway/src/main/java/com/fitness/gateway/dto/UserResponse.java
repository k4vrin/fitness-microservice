package com.fitness.gateway.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String keycloakId;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private Instant updatedAt;
}

