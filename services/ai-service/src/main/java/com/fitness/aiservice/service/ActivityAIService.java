package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.dto.GeminiResponse;
import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public GeminiResponse generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        GeminiResponse geminiResponse = geminiService.getAnswer(prompt);
        log.debug("AI RESPONSE: {}", geminiResponse);
        processAiResponse(activity, geminiResponse.candidates().getFirst().content().parts().getFirst().text());
        return geminiResponse;
    }

    private void processAiResponse(Activity activity, String aiResponse) {
        try {
            log.info("Processing AI response for activity {}: {}", activity.getId(), aiResponse);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = aiResponse
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();
            JsonNode analysisJson = objectMapper.readTree(jsonContent);


            log.info("AI response for activity {}: {}", activity.getId(), jsonContent);


        } catch (Exception e) {
            log.error("Error processing AI response for activity {}: {}", activity.getId(), e.getMessage());
        }
    }

    private String createPromptForActivity(Activity activity) {
        StringBuilder prompt = new StringBuilder(600);
        prompt.append("You are a certified fitness coach and sports nutrition assistant. ")
                .append("Generate a detailed, actionable recommendation based on the user's activity. ")
                .append("Use the activity details below, infer likely goals and training context, ")
                .append("and provide safe guidance. Avoid medical claims.\n\n")
                .append("Activity details:\n")
                .append("- Activity ID: ").append(activity.getId()).append('\n')
                .append("- User ID: ").append(activity.getUserId()).append('\n')
                .append("- Type: ").append(activity.getType()).append('\n')
                .append("- Duration (minutes): ").append(activity.getDuration()).append('\n')
                .append("- Calories burned: ").append(activity.getCaloriesBurned()).append('\n')
                .append("- Start time: ").append(activity.getStartTime()).append('\n')
                .append("- End time: ").append(activity.getEndTime()).append('\n');

        if (activity.getAdditionalMetrics() != null && !activity.getAdditionalMetrics().isEmpty()) {
            prompt.append("- Additional metrics: ").append(activity.getAdditionalMetrics()).append('\n');
        } else {
            prompt.append("- Additional metrics: none\n");
        }

        prompt.append("\nOutput format:\n")
                .append("Respond with ONLY valid JSON and EXACTLY this structure (no extra keys, no markdown):\n")
                .append("{\n")
                .append("  \"analysis\": {\n")
                .append("    \"overall\": \"Overall analysis here\",\n")
                .append("    \"pace\": \"Pace analysis here\",\n")
                .append("    \"heartRate\": \"Heart rate analysis here\",\n")
                .append("    \"caloriesBurned\": \"Calories analysis here\"\n")
                .append("  },\n")
                .append("  \"improvements\": [\n")
                .append("    {\n")
                .append("      \"area\": \"Area name\",\n")
                .append("      \"recommendation\": \"Detailed recommendation\"\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"suggestions\": [\n")
                .append("    {\n")
                .append("      \"workout\": \"Workout name\",\n")
                .append("      \"description\": \"Detailed workout description\"\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"safety\": [\n")
                .append("    \"Safety point 1\",\n")
                .append("    \"Safety point 2\"\n")
                .append("  ]\n")
                .append("}\n")
                .append("Populate the fields with realistic, activity-specific content based on the data above.");

        return prompt.toString();
    }
}
