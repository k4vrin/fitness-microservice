package com.fitness.activityservice.service;

import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository repository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;


    public Activity trackActivity(Activity activity) {
        if (!userValidationService.isValidUser(activity.getUserId())) {
            throw new RuntimeException("Invalid user ID: " + activity.getUserId());
        }

        Activity savedActivity = repository.save(activity);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        } catch (Exception e) {
            // Log the exception (logging framework assumed to be set up)
            log.error("Failed to send activity to RabbitMQ: {}", e.getMessage());
        }

        return savedActivity;
    }

    public Activity getActivity(String activityId) {
        return repository.findById(activityId).orElseThrow(() ->
                new RuntimeException("Activity not found with id: " + activityId));
    }

    public List<Activity> getActivitiesByUser(String userId) {
        return repository.findAllByUserId(userId);
    }


}
