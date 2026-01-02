package com.fitness.aiservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class Activity {

    @Id
    private String id;
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, Object> additionalMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
