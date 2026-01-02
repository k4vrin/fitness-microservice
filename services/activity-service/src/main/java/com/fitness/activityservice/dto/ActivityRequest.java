package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import lombok.Data;

import java.util.Map;

@Data
public class ActivityRequest {
    private String userId;
    private ActivityType activityType;
    private Integer duration;
    private Integer caloriesBurned;
    private String startTime;
    private String endTime;
    private Map<String, Object> additionalMetrics;

    // Mapper
    public static Activity toActivity(ActivityRequest request) {
        System.out.println(request.toString());
        return Activity.builder()
                .userId(request.getUserId())
                .type(request.getActivityType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(java.time.LocalDateTime.parse(request.getStartTime()))
                .endTime(java.time.LocalDateTime.parse(request.getEndTime()))
                .additionalMetrics(request.getAdditionalMetrics())
                .build();
    }
}
