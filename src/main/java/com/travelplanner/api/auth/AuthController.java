package com.travelplanner.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, login, token refresh and logout. All endpoints are public — no token required.")
@SecurityRequirements // Override global security: these endpoints are public
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new account",
            description = "Creates a new user account and returns a ready-to-use JWT access token and a refresh token. No prior authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — check the request body fields",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
            @ApiResponse(responseCode = "409", description = "Email is already registered",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(
            summary = "Login with email and password",
            description = "Authenticates the user and returns a new JWT access token (1 day) and refresh token (7 days). Previous tokens are revoked."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Operation(
            summary = "Refresh the access token",
            description = "Uses a valid refresh token to issue a new access token without requiring the user to log in again. Call this silently from an Angular HTTP Interceptor when the access token expires."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access token issued",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Refresh token missing or malformed",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "Refresh token is expired or invalid",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "Bearer <refresh_token>", required = true, example = "Bearer eyJhbGci...")
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return ResponseEntity.ok(authService.refreshToken(authHeader));
    }

    @Operation(
            summary = "Logout",
            description = "Invalidates the current access token immediately. The token is marked as revoked in the database and will be rejected on all subsequent requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Token missing or already invalid")
    })
    @PostMapping("/logout")
    public void logout() {
        // Handled by Spring Security LogoutFilter + LogoutService
    }
}
