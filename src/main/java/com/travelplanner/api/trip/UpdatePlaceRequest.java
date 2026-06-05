package com.travelplanner.api.trip;

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
@Schema(description = "User-editable fields on a place. The AI-generated content (name, coordinates, photos, prices) cannot be changed.")
public class UpdatePlaceRequest {

    @Schema(description = "Mark this place as visited", example = "true")
    private Boolean visited;

    @Schema(description = "Personal note about this place (written after the visit)", example = "Amazing sunset view from the top!")
    @Size(max = 1000, message = "Personal note must be under 1000 characters")
    private String personalNote;
}
