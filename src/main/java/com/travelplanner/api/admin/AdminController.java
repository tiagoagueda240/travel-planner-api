package com.travelplanner.api.admin;

import com.travelplanner.api.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints restricted to **ROLE_ADMIN**. Returns `403 Forbidden` for regular users.")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Application statistics",
            description = "Returns global counters useful for an admin dashboard: total registered users and total generated trips.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats returned"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    @GetMapping("/stats")
    public ResponseEntity<AdminService.AppStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @Operation(summary = "List all users (paginated)",
            description = "Returns a paginated list of all registered users. Useful for a user management table in an admin panel.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User list returned"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN")
    })
    @GetMapping("/users")
    public ResponseEntity<Page<User>> listUsers(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page (max 100)", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listUsers(PageRequest.of(page, Math.min(size, 100))));
    }

    @Operation(summary = "Get user by ID",
            description = "Returns the full profile of a specific user by their database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", example = "5") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @Operation(summary = "Delete a user account",
            description = "Permanently deletes a user account and all their trips. **This action is irreversible.** All the user's tokens are also invalidated.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "403", description = "Requires ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", example = "5") @PathVariable Integer id
    ) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}


    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminService.AppStats> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminService.listUsers(PageRequest.of(page, Math.min(size, 100))));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
