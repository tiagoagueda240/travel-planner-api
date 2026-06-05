package com.travelplanner.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT tokens returned after a successful login or registration")
public class AuthResponse {

    @JsonProperty("access_token")
    @Schema(description = "JWT access token — valid for 1 day. Send in every request as: Authorization: Bearer <token>",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @JsonProperty("refresh_token")
    @Schema(description = "Refresh token — valid for 7 days. Use POST /auth/refresh-token to silently renew the access token.",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;
}
