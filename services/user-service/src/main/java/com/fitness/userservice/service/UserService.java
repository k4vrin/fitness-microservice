package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.UserEntity;
import com.fitness.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserResponse.fromEntity(userEntity);
    }


    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            UserEntity userEntity = userRepository.findByEmail(request.getEmail());
            return UserResponse.fromEntity(userEntity);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(request.getEmail());
        userEntity.setFirstName(request.getFirstName());
        userEntity.setKeycloakId(request.getKeycloakId());
        userEntity.setLastName(request.getLastName());
        userEntity.setPassword(request.getPassword());

        UserEntity savedUserEntity = userRepository.save(userEntity);

        return UserResponse.fromEntity(savedUserEntity);

    }

    public boolean validateUser(String keycloakId) {
        log.info("Validating user {}", keycloakId);
        return userRepository.existsByKeycloakId(keycloakId);
    }
}
