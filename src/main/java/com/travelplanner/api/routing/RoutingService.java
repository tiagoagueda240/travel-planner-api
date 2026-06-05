package com.travelplanner.api.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoutingService {

    private final WebClient osrmWebClient;
    private final ObjectMapper objectMapper;

    public RoutingService(
            @Qualifier("osrmWebClient") WebClient osrmWebClient,
            ObjectMapper objectMapper
    ) {
        this.osrmWebClient = osrmWebClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Gets a road-following GeoJSON LineString for a list of waypoints.
     * Uses the public OSRM demo server (free, no API key required).
     *
     * @param coordinates list of [lat, lng] pairs
     * @return GeoJSON geometry string or null if routing fails
     */
    public String getRouteGeoJson(List<double[]> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }

        // OSRM format: lng,lat;lng,lat (longitude first)
        String coordString = coordinates.stream()
                .map(c -> c[1] + "," + c[0])
                .collect(Collectors.joining(";"));

        try {
            String response = osrmWebClient.get()
                    .uri("/route/v1/driving/{coords}?overview=full&geometries=geojson", coordString)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode root = objectMapper.readTree(response);
            String code = root.path("code").asText();

            if (!"Ok".equals(code)) {
                log.warn("OSRM returned non-OK code: {}", code);
                return null;
            }

            JsonNode geometry = root.path("routes").get(0).path("geometry");
            return objectMapper.writeValueAsString(geometry);

        } catch (Exception e) {
            log.warn("Route calculation failed (non-critical): {}", e.getMessage());
            return null; // Route is optional — the map can still show markers without a route
        }
    }
}
