package com.travelplanner.api.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    @Value("${application.ai.gemini.api-key}")
    private String apiKey;

    @Value("${application.ai.gemini.model}")
    private String model;

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    public GeminiService(
            @Qualifier("geminiWebClient") WebClient geminiWebClient,
            ObjectMapper objectMapper
    ) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = objectMapper;
    }

    public GeminiItinerary generateItinerary(String destination, String travelType, int days, String language) {
        String sanitizedDestination = sanitizeInput(destination);
        String sanitizedTravelType = sanitizeInput(travelType);
        String prompt = buildItineraryPrompt(sanitizedDestination, sanitizedTravelType, days, language);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", prompt))
                        )
                ),
                "tools", List.of(
                        Map.of("google_search", Map.of())
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", 8192
                )
        );

        try {
            String rawResponse = geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseResponse(rawResponse);

        } catch (WebClientResponseException e) {
            log.error("Gemini API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI service error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to call Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate itinerary: " + e.getMessage());
        }
    }

    private GeminiItinerary parseResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

            String jsonText = extractJsonObject(text);
            return objectMapper.readValue(jsonText, GeminiItinerary.class);

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            log.debug("Raw response was: {}", rawResponse);
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage());
        }
    }

    // Extracts the first complete JSON object found in the text
    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        if (start == -1) {
            throw new RuntimeException("No JSON object found in AI response");
        }

        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }

        throw new RuntimeException("Malformed JSON in AI response");
    }

    /**
     * Strips characters commonly used for prompt injection before inserting
     * user-supplied values into the LLM prompt.
     */
    private String sanitizeInput(String input) {
        if (input == null) return "";
        // Remove instruction-delimiter patterns and excessive whitespace
        return input
                .replaceAll("(?i)(ignore|forget|disregard|system|assistant|user)\\s*:", "")
                .replaceAll("[\\[\\]{}<>|`]", "")
                .replaceAll("\\s{3,}", " ")
                .trim();
    }

    private String buildItineraryPrompt(String destination, String travelType, int days, String language) {
        String languageInstruction = "en".equals(language)
                ? "Write all descriptions in English."
                : "Write all name fields in the local language but write all description fields in the language with ISO code: " + language + ".";

        return """
                Use Google Search to find the best and most up-to-date recommendations for a trip to %s
                with a focus on %s over %d days.

                Create a logical itinerary, grouping places by geographic zones so that daily travel
                is efficient without zigzagging. For each place, provide exact Latitude and Longitude coordinates.

                %s

                Return ONLY a strict JSON object with the following structure (no text before or after):
                {
                  "days": [
                    {
                      "day": 1,
                      "places": [
                        {
                          "name": "...",
                          "description": "A short, engaging description (2-3 sentences).",
                          "lat": 12.34,
                          "lng": 56.78,
                          "type": "monument|restaurant|museum|park|beach|shopping|entertainment",
                          "photos": [{"url": "https://..."}],
                          "price": {"kids": 0, "students": 0, "adults": 0}
                        }
                      ]
                    }
                  ]
                }

                Rules:
                - Include 4-6 places per day, grouped by geographic proximity
                - Set price to 0 for free locations
                - For photo URLs, use real publicly accessible image URLs if available, otherwise omit
                - The "type" field must be one of: monument, restaurant, museum, park, beach, shopping, entertainment
                """.formatted(destination, travelType, days, languageInstruction);
    }
}
