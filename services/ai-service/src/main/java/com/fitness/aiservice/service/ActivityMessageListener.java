package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService activityAIService;
    private final RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void processActivityMessage(Activity activity) {
        log.info("Received activity message: {}", activity.getId());
        try {
            Recommendation recommendation = activityAIService.generateRecommendation(activity);
            recommendationRepository.save(recommendation);
        } catch (WebClientRequestException ex) {
            log.warn("Skipping recommendation for activity {} due to Gemini connectivity issue: {}",
                    activity.getId(), ex.getMessage());
        } catch (Exception e) {
            log.error("Error while processing activity message", e);
        }
    }
}
