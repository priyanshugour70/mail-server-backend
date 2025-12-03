package com.lssgoo.mail.controller;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.dtos.request.CreateOrganisationRequest;
import com.lssgoo.mail.dtos.request.UpdateOrganisationRequest;
import com.lssgoo.mail.dtos.response.OrganisationResponse;
import com.lssgoo.mail.service.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organisations")
@Tag(name = "Organisations", description = "Organisation management APIs")
public class OrganisationController {

    @Autowired
    private OrganisationService organisationService;

    @Operation(summary = "Create a new organisation", description = "Creates a new organisation. This endpoint is open and does not require authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation created successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation creation failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping
    public ResponseEntity<APIResponse<OrganisationResponse>> createOrganisation(
            @Valid @RequestBody CreateOrganisationRequest request,
            HttpServletRequest httpRequest) {
        try {
            OrganisationResponse response = organisationService.createOrganisation(request);
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(true)
                    .message("Organisation created successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get organisation by ID", description = "Retrieves organisation details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<OrganisationResponse>> getOrganisationById(@PathVariable Long id) {
        try {
            OrganisationResponse response = organisationService.getOrganisationById(id);
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(true)
                    .message("Organisation retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get organisation by name", description = "Retrieves organisation details by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/name/{name}")
    public ResponseEntity<APIResponse<OrganisationResponse>> getOrganisationByName(@PathVariable String name) {
        try {
            OrganisationResponse response = organisationService.getOrganisationByName(name);
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(true)
                    .message("Organisation retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get organisation by domain", description = "Retrieves organisation details by domain")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/domain/{domain}")
    public ResponseEntity<APIResponse<OrganisationResponse>> getOrganisationByDomain(@PathVariable String domain) {
        try {
            OrganisationResponse response = organisationService.getOrganisationByDomain(domain);
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(true)
                    .message("Organisation retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get all organisations", description = "Retrieves all organisations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<APIResponse<List<OrganisationResponse>>> getAllOrganisations() {
        try {
            List<OrganisationResponse> response = organisationService.getAllOrganisations();
            return ResponseEntity.ok(APIResponse.<List<OrganisationResponse>>builder()
                    .success(true)
                    .message("Organisations retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<List<OrganisationResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get all active organisations", description = "Retrieves all active organisations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active organisations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/active")
    public ResponseEntity<APIResponse<List<OrganisationResponse>>> getAllActiveOrganisations() {
        try {
            List<OrganisationResponse> response = organisationService.getAllActiveOrganisations();
            return ResponseEntity.ok(APIResponse.<List<OrganisationResponse>>builder()
                    .success(true)
                    .message("Active organisations retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<List<OrganisationResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Update organisation", description = "Updates an existing organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation updated successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation update failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<OrganisationResponse>> updateOrganisation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganisationRequest request) {
        try {
            OrganisationResponse response = organisationService.updateOrganisation(id, request);
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(true)
                    .message("Organisation updated successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<OrganisationResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Delete organisation", description = "Deletes an organisation (only if it has no users)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation deleted successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation deletion failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteOrganisation(@PathVariable Long id) {
        try {
            organisationService.deleteOrganisation(id);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Organisation deleted successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Activate organisation", description = "Activates an organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation activated successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation activation failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/activate")
    public ResponseEntity<APIResponse<Void>> activateOrganisation(@PathVariable Long id) {
        try {
            organisationService.activateOrganisation(id);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Organisation activated successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Deactivate organisation", description = "Deactivates an organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation deactivated successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Organisation deactivation failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<APIResponse<Void>> deactivateOrganisation(@PathVariable Long id) {
        try {
            organisationService.deactivateOrganisation(id);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Organisation deactivated successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}

