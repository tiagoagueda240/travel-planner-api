package com.travelplanner.api.trip;

import com.travelplanner.api.models.TripStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Editable metadata fields of a trip. All fields are optional — only provided fields are updated.")
public class UpdateTripRequest {

    @Schema(description = "Custom name for this trip", example = "Summer in Lisbon 2026")
    @Size(max = 200, message = "Title must be under 200 characters")
    private String title;

    @Schema(description = "Personal notes about the trip", example = "Don't forget the sunscreen!")
    @Size(max = 2000, message = "Notes must be under 2000 characters")
    private String notes;

    @Schema(description = "Current planning status of the trip", example = "COMPLETED",
            allowableValues = {"PLANNING", "ACTIVE", "COMPLETED"})
    private TripStatus status;
}
