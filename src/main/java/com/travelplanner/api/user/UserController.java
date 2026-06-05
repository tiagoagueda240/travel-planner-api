package com.travelplanner.api.user;

import com.travelplanner.api.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Manage the authenticated user's profile, password and account. All endpoints require a valid Bearer token.")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile",
            description = "Returns the full profile of the currently authenticated user, including their preferred language for itinerary generation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update profile",
            description = "Updates one or more profile fields. All fields are optional — only the fields present in the body are updated. " +
                    "Changing `preferredLanguage` affects the language of future AI-generated itineraries.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "New email is already in use")
    })
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(user.getEmail(), request));
    }

    @Operation(summary = "Change password",
            description = "Changes the user's password. Requires the current password for verification. " +
                    "After a successful change, all existing tokens remain valid — the user does NOT need to log in again.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Current password incorrect, or new passwords do not match")
    })
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        userService.changePassword(user.getEmail(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete account",
            description = "Permanently deletes the authenticated user's account and all their trips. " +
                    "Also invalidates all existing tokens. **This action is irreversible.**")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userService.deleteAccount(user.getEmail());
        return ResponseEntity.noContent().build();
    }
}
