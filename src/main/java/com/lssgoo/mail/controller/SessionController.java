package com.lssgoo.mail.controller;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.dtos.response.SessionResponse;
import com.lssgoo.mail.service.SessionService;
import com.lssgoo.mail.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Sessions", description = "Session management and activity tracking APIs")
public class SessionController {

    private static final Logger logger = LoggerUtil.getLogger(SessionController.class);

    @Autowired
    private SessionService sessionService;

    @Operation(summary = "Get current session", description = "Retrieves the current active session with all activities and details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/current")
    public ResponseEntity<APIResponse<SessionResponse>> getCurrentSession(HttpServletRequest httpRequest) {
        logger.info("Get current session request received");
        try {
            SessionResponse response = sessionService.getCurrentSession(httpRequest);
            logger.info("Current session retrieved successfully - Session ID: {}", response.getId());
            return ResponseEntity.ok(APIResponse.<SessionResponse>builder()
                    .success(true)
                    .message("Current session retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get current session - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<SessionResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get session by ID", description = "Retrieves a specific session by ID with all activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<SessionResponse>> getSessionById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        logger.info("Get session by ID request received: {}", id);
        try {
            SessionResponse response = sessionService.getSessionById(id, httpRequest);
            logger.info("Session retrieved successfully: ID {}", id);
            return ResponseEntity.ok(APIResponse.<SessionResponse>builder()
                    .success(true)
                    .message("Session retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get session by ID: {} - Error: {}", id, e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<SessionResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get all user sessions", description = "Retrieves all sessions for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<APIResponse<List<SessionResponse>>> getAllUserSessions(HttpServletRequest httpRequest) {
        logger.info("Get all user sessions request received");
        try {
            List<SessionResponse> response = sessionService.getAllUserSessions(httpRequest);
            logger.info("Retrieved {} user sessions", response.size());
            return ResponseEntity.ok(APIResponse.<List<SessionResponse>>builder()
                    .success(true)
                    .message("Sessions retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get all user sessions - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<List<SessionResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get active user sessions", description = "Retrieves all active sessions for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active sessions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/active")
    public ResponseEntity<APIResponse<List<SessionResponse>>> getActiveUserSessions(HttpServletRequest httpRequest) {
        logger.info("Get active user sessions request received");
        try {
            List<SessionResponse> response = sessionService.getActiveUserSessions(httpRequest);
            logger.info("Retrieved {} active user sessions", response.size());
            return ResponseEntity.ok(APIResponse.<List<SessionResponse>>builder()
                    .success(true)
                    .message("Active sessions retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get active user sessions - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<List<SessionResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Update session status", description = "Updates the session status checked timestamp (used to track active/inactive status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session status updated successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Session status update failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/status")
    public ResponseEntity<APIResponse<Void>> updateSessionStatus(HttpServletRequest httpRequest) {
        logger.info("Update session status request received");
        try {
            sessionService.updateSessionStatus(httpRequest);
            logger.info("Session status updated successfully");
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Session status updated successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to update session status - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}

