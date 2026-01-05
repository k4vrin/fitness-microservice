package com.fitness.userservice.dto;

import com.fitness.userservice.model.UserEntity;
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

    // Mapper method to convert from UserEntity to UserResponse
    public static UserResponse fromEntity(UserEntity userEntity) {
        UserResponse response = new UserResponse();
        response.setId(userEntity.getId());
        response.setEmail(userEntity.getEmail());
        response.setKeycloakId(userEntity.getKeycloakId());
        response.setFirstName(userEntity.getFirstName());
        response.setLastName(userEntity.getLastName());
        response.setCreatedAt(userEntity.getCreatedAt());
        response.setUpdatedAt(userEntity.getUpdatedAt());
        return response;
    }
}

