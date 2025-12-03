package com.lssgoo.mail.controller;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.dtos.request.*;
import com.lssgoo.mail.dtos.response.AuthResponse;
import com.lssgoo.mail.dtos.response.TokenResponse;
import com.lssgoo.mail.dtos.response.UserResponse;
import com.lssgoo.mail.service.AuthService;
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

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and returns authentication tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Registration failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<APIResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.register(request, httpRequest);
            return ResponseEntity.ok(APIResponse.<AuthResponse>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<AuthResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Login failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.login(request, httpRequest);
            return ResponseEntity.ok(APIResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<AuthResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Refresh access token", description = "Generates new access and refresh tokens using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Token refresh failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(APIResponse.<TokenResponse>builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<TokenResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "User logout", description = "Logs out the current user session or all sessions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Logout failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request == null) {
                request = new LogoutRequest();
            }
            authService.logout(request, httpRequest);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Logout successful")
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

    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Failed to retrieve user",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserResponse>> getCurrentUser(HttpServletRequest httpRequest) {
        try {
            UserResponse response = authService.getCurrentUser(httpRequest);
            return ResponseEntity.ok(APIResponse.<UserResponse>builder()
                    .success(true)
                    .message("User retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(APIResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Change password", description = "Changes the password for the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "200", description = "Password change failed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ResponseEntity<APIResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            authService.changePassword(request, httpRequest);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Password changed successfully")
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

