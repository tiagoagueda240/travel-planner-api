package com.travelplanner.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile fields to update. All fields are optional — only provided fields are changed.")
public class UpdateProfileRequest {

    @Schema(description = "First name", example = "João")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Silva")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Schema(description = "New email address (must be unique)", example = "joao.novo@email.com")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "URL to a profile photo", example = "https://cdn.example.com/avatar/joao.jpg")
    private String profilePhotoUrl;

    @Schema(
            description = "ISO 639-1 language code for AI-generated itinerary descriptions",
            example = "pt",
            allowableValues = {"en", "pt", "es", "fr", "de", "it", "nl", "ja", "zh", "ar"}
    )
    @Pattern(
            regexp = "en|pt|es|fr|de|it|nl|ja|zh|ar",
            message = "preferredLanguage must be a valid ISO 639-1 code (en, pt, es, fr, de, it, nl, ja, zh, ar)"
    )
    private String preferredLanguage;
}
