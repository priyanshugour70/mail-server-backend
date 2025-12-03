package com.lssgoo.mail.controller;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.dtos.request.*;
import com.lssgoo.mail.dtos.response.AuthResponse;
import com.lssgoo.mail.dtos.response.TokenResponse;
import com.lssgoo.mail.dtos.response.UserResponse;
import com.lssgoo.mail.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        try {
            AuthResponse response = authService.register(request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(APIResponse.<AuthResponse>builder()
                            .success(true)
                            .message("User registered successfully")
                            .data(response)
                            .timestamp(LocalDateTime.now())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.<AuthResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.<AuthResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.<TokenResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResponse.<UserResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }
}

