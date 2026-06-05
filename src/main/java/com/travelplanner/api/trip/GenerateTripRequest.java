package com.travelplanner.api.trip;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parameters for generating a new AI travel itinerary")
public class GenerateTripRequest {

    @Schema(description = "Destination city or region", example = "Lisboa, Portugal")
    @NotBlank(message = "Destination is required")
    @Size(max = 200, message = "Destination must be under 200 characters")
    private String destination;

    @Schema(
            description = "Travel style — determines what kind of places Gemini prioritises",
            example = "cultural",
            allowableValues = {"cultural", "adventure", "gastronomy", "family", "romantic", "nature", "beach", "city", "wellness"}
    )
    @NotBlank(message = "Travel type is required")
    @Pattern(
            regexp = "cultural|adventure|gastronomy|family|romantic|nature|beach|city|wellness",
            message = "travelType must be one of: cultural, adventure, gastronomy, family, romantic, nature, beach, city, wellness"
    )
    private String travelType;

    @Schema(description = "Number of days for the trip (1–30)", example = "3")
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Trip must be at least 1 day")
    @Max(value = 30, message = "Trip cannot exceed 30 days")
    private Integer durationDays;
}

