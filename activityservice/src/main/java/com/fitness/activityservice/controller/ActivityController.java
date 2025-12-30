package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest activity) {
        Activity act = activityService.trackActivity(ActivityRequest.toActivity(activity));
        return ResponseEntity.ok(ActivityResponse.from(act));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-Id") String userId) {
        List<Activity> activities = activityService.getActivitiesByUser(userId);
        List<ActivityResponse> response = activities.stream()
                .map(ActivityResponse::from)
                .toList();
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId) {
        Activity act = activityService.getActivity(activityId);
        return ResponseEntity.ok(ActivityResponse.from(act));
    }
}
