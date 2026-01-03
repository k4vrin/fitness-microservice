package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.dto.GeminiResponse;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        GeminiResponse geminiResponse = geminiService.getAnswer(prompt);
        log.debug("AI RESPONSE: {}", geminiResponse.responseId());
        Recommendation recommendation = processAiResponse(activity, geminiResponse.candidates().getFirst().content().parts().getFirst().text());
        return recommendation;
    }

    private Recommendation processAiResponse(Activity activity, String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            log.warn("Empty AI response for activity {}", activity.getId());
            return createDefaultRecommendation(activity);
        }

        try {
            log.info("Processing AI response for activity {}: {}", activity.getId(), aiResponse);
            String jsonContent = extractJsonPayload(aiResponse);
            if (jsonContent == null) {
                log.warn("Failed to extract JSON payload for activity {}", activity.getId());
                return createDefaultRecommendation(activity);
            }
            JsonNode recommendationJson = OBJECT_MAPPER.readTree(jsonContent);
            JsonNode analysisNode = recommendationJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall: ");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace: ");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate: ");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories Burned: ");

            List<String> improvements = extractImprovements(recommendationJson.path("improvements"));
            List<String> suggestions = extractSuggestions(recommendationJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(recommendationJson.path("safety"));

            Recommendation recommendation = Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType().name())
                    .recommendationText(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safetyTips(safety)
                    .build();

            log.info("Processing AI response for activity {}: {}", activity.getId(), recommendation);

            return recommendation;

        } catch (Exception e) {
            log.error("Error processing AI response for activity {}: {}", activity.getId(), e.getMessage());
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType().name())
                .recommendationText("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safetyTips(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(node -> {
                addIfNotBlank(safety, node.asText());
            });
        } else if (safetyNode.isTextual()) {
            addIfNotBlank(safety, safetyNode.asText());
        }
        return safety.isEmpty() ? Collections.singletonList("Follow general safety guidelines") : safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(node -> appendSuggestion(suggestions, node));
        } else if (suggestionsNode.isObject() || suggestionsNode.isTextual()) {
            appendSuggestion(suggestions, suggestionsNode);
        }

        return suggestions.isEmpty() ? Collections.singletonList("No specific improvements provided") : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(node -> appendImprovement(improvements, node));
        } else if (improvementsNode.isObject() || improvementsNode.isTextual()) {
            appendImprovement(improvements, improvementsNode);
        }

        return improvements.isEmpty() ? Collections.singletonList("No specific suggestions provided") : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            String text = analysisNode.path(key).asText().trim();
            if (!text.isBlank()) {
                fullAnalysis.append(prefix)
                        .append(text)
                        .append("\n\n");
            }
        }
    }

    private void appendImprovement(List<String> improvements, JsonNode node) {
        if (node.isTextual()) {
            addIfNotBlank(improvements, node.asText());
            return;
        }
        String area = node.path("area").asText().trim();
        String rec = node.path("recommendation").asText().trim();
        if (!area.isEmpty() || !rec.isEmpty()) {
            improvements.add(String.format("%s,%s", area, rec));
        }
    }

    private void appendSuggestion(List<String> suggestions, JsonNode node) {
        if (node.isTextual()) {
            addIfNotBlank(suggestions, node.asText());
            return;
        }
        String workout = node.path("workout").asText().trim();
        String description = node.path("description").asText().trim();
        if (!workout.isEmpty() || !description.isEmpty()) {
            suggestions.add(String.format("%s,%s", workout, description));
        }
    }

    private void addIfNotBlank(List<String> target, String value) {
        if (value != null && !value.isBlank()) {
            target.add(value.trim());
        }
    }

    private String extractJsonPayload(String aiResponse) {
        String trimmed = aiResponse.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String noFence = stripMarkdownFence(trimmed);
        String candidate = findJsonObject(noFence);
        if (candidate != null) {
            return candidate;
        }
        return trimmed.startsWith("{") ? trimmed : null;
    }

    private String stripMarkdownFence(String input) {
        if (!input.startsWith("```")) {
            return input;
        }
        int firstLineBreak = input.indexOf('\n');
        if (firstLineBreak == -1) {
            return input;
        }
        String withoutFirstFence = input.substring(firstLineBreak + 1);
        int lastFence = withoutFirstFence.lastIndexOf("```");
        if (lastFence == -1) {
            return withoutFirstFence.trim();
        }
        return withoutFirstFence.substring(0, lastFence).trim();
    }

    private String findJsonObject(String input) {
        int start = input.indexOf('{');
        if (start < 0) {
            return null;
        }
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = start; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }
            if (ch == '"') {
                inString = true;
                continue;
            }
            if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return input.substring(start, i + 1).trim();
                }
            }
        }
        return null;
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
