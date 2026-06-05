package com.travelplanner.api.ai;

import tools.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiItinerary {

    private List<GeminiDay> days;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiDay {
        private int day;
        private List<GeminiPlace> places;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiPlace {
        private String name;
        private String description;
        private double lat;
        private double lng;
        private String type;
        private List<GeminiPhoto> photos;
        private GeminiPrice price;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiPhoto {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiPrice {
        private int kids;
        private int students;
        private int adults;
    }
}
