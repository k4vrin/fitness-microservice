package com.fitness.activityservice.service;

import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository repository;
    private final UserValidationService userValidationService;


    public Activity trackActivity(Activity activity) {
        if (!userValidationService.isValidUser(activity.getUserId())) {
            throw new RuntimeException("Invalid user ID: " + activity.getUserId());
        }
        return repository.save(activity);
    }

    public Activity getActivity(String activityId) {
        return repository.findById(activityId).orElseThrow(() ->
                new RuntimeException("Activity not found with id: " + activityId));
    }

    public List<Activity> getActivitiesByUser(String userId) {
        return repository.findAllByUserId(userId);
    }


}
