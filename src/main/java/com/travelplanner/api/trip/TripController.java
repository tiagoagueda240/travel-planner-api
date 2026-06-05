package com.travelplanner.api.trip;

import com.travelplanner.api.models.Day;
import com.travelplanner.api.models.Place;
import com.travelplanner.api.models.Trip;
import com.travelplanner.api.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Generate and manage AI-powered travel itineraries. Each trip has days, each day has places with GPS coordinates and a GeoJSON road route for MapLibre animation.")
public class TripController {

    private final TripService tripService;

    // ── Trip CRUD ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Generate a new AI itinerary",
            description = """
                    Calls **Gemini AI with Google Search Grounding** to generate a real, up-to-date itinerary.

                    The AI groups places by geographic proximity to avoid daily zigzag routes.
                    After the AI response, the API calls **OSRM** (free, no key needed) to convert GPS waypoints
                    into a real road-following GeoJSON LineString for each day — ready to animate on MapLibre.

                    The itinerary language is determined by the user's `preferredLanguage` profile setting.

                    ⚠️ This endpoint calls external APIs and may take **5-15 seconds**.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Itinerary generated and saved",
                    content = @Content(schema = @Schema(implementation = Trip.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request — check destination, travelType and durationDays"),
            @ApiResponse(responseCode = "500", description = "AI service error — Gemini API key missing or quota exceeded")
    })
    @PostMapping("/generate")
    public ResponseEntity<Trip> generateTrip(
            Authentication authentication,
            @Valid @RequestBody GenerateTripRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.generateAndSave(request, user));
    }

    @Operation(
            summary = "List my trips (paginated)",
            description = "Returns a paginated list of all trips belonging to the authenticated user, ordered by creation date descending (newest first)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of trips returned")
    })
    @GetMapping
    public ResponseEntity<Page<Trip>> getUserTrips(
            Authentication authentication,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page (max 50)", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(tripService.getUserTrips(user, pageable));
    }

    @Operation(
            summary = "Get a full trip by ID",
            description = "Returns the complete trip including all days, places, GPS coordinates, photo URLs, prices and road route GeoJSON. This is the main payload used to render the map and bottom sheet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trip found",
                    content = @Content(schema = @Schema(implementation = Trip.class))),
            @ApiResponse(responseCode = "404", description = "Trip not found or does not belong to this user")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(
            Authentication authentication,
            @Parameter(description = "Trip ID", example = "1") @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(tripService.getTripById(id, user));
    }

    @Operation(
            summary = "Update trip metadata",
            description = "Updates the trip's user-editable fields: `title`, `notes` and `status`. All fields are optional — only the fields present in the body are changed. The AI-generated content (places, routes) is never modified by this endpoint."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trip updated",
                    content = @Content(schema = @Schema(implementation = Trip.class))),
            @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(
            Authentication authentication,
            @Parameter(description = "Trip ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateTripRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(tripService.updateTrip(id, user, request));
    }

    @Operation(
            summary = "Delete a trip",
            description = "Permanently deletes the trip and all its days and places. **This action is irreversible.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Trip deleted"),
            @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(
            Authentication authentication,
            @Parameter(description = "Trip ID", example = "1") @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();
        tripService.deleteTrip(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── Day ───────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Get a specific day",
            description = "Returns a single day with its full list of places and the `routeGeoJson` LineString geometry. Useful when the Angular component switches between days in the bottom sheet without re-fetching the entire trip."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Day found",
                    content = @Content(schema = @Schema(implementation = Day.class))),
            @ApiResponse(responseCode = "404", description = "Day or trip not found")
    })
    @GetMapping("/{tripId}/days/{dayId}")
    public ResponseEntity<Day> getDay(
            Authentication authentication,
            @Parameter(description = "Trip ID", example = "1") @PathVariable Long tripId,
            @Parameter(description = "Day ID", example = "10") @PathVariable Long dayId
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(tripService.getDayById(tripId, dayId, user));
    }

    // ── Place ─────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Update a place (visited / personal note)",
            description = "Lets the user mark a place as visited and/or add a personal note. These are the only user-editable fields on a place — the AI-generated content (name, description, coordinates, photos, prices) is never modified."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Place updated",
                    content = @Content(schema = @Schema(implementation = Place.class))),
            @ApiResponse(responseCode = "404", description = "Place, day or trip not found")
    })
    @PatchMapping("/{tripId}/days/{dayId}/places/{placeId}")
    public ResponseEntity<Place> updatePlace(
            Authentication authentication,
            @Parameter(description = "Trip ID", example = "1") @PathVariable Long tripId,
            @Parameter(description = "Day ID", example = "10") @PathVariable Long dayId,
            @Parameter(description = "Place ID", example = "100") @PathVariable Long placeId,
            @Valid @RequestBody UpdatePlaceRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(tripService.updatePlace(tripId, dayId, placeId, user, request));
    }
}
