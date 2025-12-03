package com.lssgoo.mail.service;

import com.lssgoo.mail.dtos.request.*;
import com.lssgoo.mail.dtos.response.AuthResponse;
import com.lssgoo.mail.dtos.response.TokenResponse;
import com.lssgoo.mail.dtos.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request, HttpServletRequest httpRequest);

    UserResponse getCurrentUser(HttpServletRequest httpRequest);

    void changePassword(ChangePasswordRequest request, HttpServletRequest httpRequest);
}

