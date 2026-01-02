package com.fitness.aiservice.dto;

import java.util.List;

public record GeminiRequest(List<Content> contents) {

    public static GeminiRequest forPrompt(String prompt) {
        return new GeminiRequest(List.of(new Content(List.of(new Part(prompt)))));
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }
}
