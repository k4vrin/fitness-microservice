package com.fitness.aiservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("recommendations")
@Data
@Builder
public class Recommendation {

    @Id
    private String id;
    private String userId;
    private String activityId;
    private String activityType;
    private String recommendationText;
    private List<String> improvements;
    private List<String> suggestions;
    private List<String> safetyTips;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
