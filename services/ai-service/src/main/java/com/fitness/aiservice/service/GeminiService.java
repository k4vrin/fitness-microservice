package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.GeminiRequest;
import com.fitness.aiservice.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public GeminiResponse getAnswer(String prompt) {
        GeminiRequest requestBody = GeminiRequest.forPrompt(prompt);

        return webClient.post()
                .uri(apiUrl)
                .header("X-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();
    }


}
