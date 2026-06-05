package com.travelplanner.api.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Password change request — requires the current password for verification")
public class ChangePasswordRequest {

    @Schema(description = "The user's current password for verification", example = "OldPassword@123")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "The new password — minimum 8 characters", example = "NewPassword@456")
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;

    @Schema(description = "Repeat the new password — must match newPassword", example = "NewPassword@456")
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
