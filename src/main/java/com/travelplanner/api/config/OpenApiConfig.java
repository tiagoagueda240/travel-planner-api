package com.travelplanner.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI travelPlannerOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Travel Planner API")
                        .version("1.0.0")
                        .description("""
                                ## AI-Powered Travel Itinerary Generator
                                
                                This API powers an intelligent travel planning application that uses **Gemini AI with Search Grounding**
                                to generate real, up-to-date travel itineraries grouped by geographic zones.
                                
                                ### Authentication Flow
                                1. Register via `POST /api/v1/auth/register` or login via `POST /api/v1/auth/login`
                                2. Copy the `access_token` from the response
                                3. Click **Authorize** above and paste: `<your_token>`
                                4. All authenticated endpoints will now work
                                
                                ### Token Lifecycle
                                - **Access token**: valid for **1 day**
                                - **Refresh token**: valid for **7 days** — use `POST /api/v1/auth/refresh-token` to get a new access token silently
                                """)
                        .contact(new Contact()
                                .name("Travel Planner")
                                .email("support@travelplanner.app"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Frontend Angular Repository")
                        .url("https://github.com/travelplanner/frontend"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.travelplanner.app").description("Production")
                ))
                .tags(List.of(
                        new Tag().name("Auth").description("Register, login, token refresh and logout. All endpoints are public."),
                        new Tag().name("User").description("Manage the authenticated user's profile, password and account."),
                        new Tag().name("Trips").description("Generate and manage AI-powered travel itineraries. Each trip contains days, and each day contains places with map coordinates and road routes."),
                        new Tag().name("Admin").description("Administrative endpoints — restricted to users with **ROLE_ADMIN**.")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the `access_token` received from /auth/login or /auth/register")));
    }
}
