package com.lssgoo.mail.service.impl;

import com.lssgoo.mail.dtos.request.*;
import com.lssgoo.mail.dtos.response.AuthResponse;
import com.lssgoo.mail.dtos.response.SessionResponse;
import com.lssgoo.mail.dtos.response.TokenResponse;
import com.lssgoo.mail.dtos.response.UserResponse;
import com.lssgoo.mail.entity.AuditLog;
import com.lssgoo.mail.entity.Organisation;
import com.lssgoo.mail.entity.Session;
import com.lssgoo.mail.entity.User;
import com.lssgoo.mail.repository.AuditLogRepository;
import com.lssgoo.mail.repository.OrganisationRepository;
import com.lssgoo.mail.repository.SessionRepository;
import com.lssgoo.mail.repository.UserRepository;
import com.lssgoo.mail.security.jwt.JwtTokenProvider;
import com.lssgoo.mail.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setIsActive(true);
        user.setIsEmailVerified(false);

        // Set organisation if provided
        if (request.getOrganisationId() != null) {
            Organisation organisation = organisationRepository.findById(request.getOrganisationId())
                    .orElseThrow(() -> new RuntimeException("Organisation not found"));
            user.setOrganisation(organisation);
        }

        user = userRepository.save(user);

        // Create audit log
        createAuditLog(user, null, "USER_REGISTERED", "User", user.getId(),
                "User registered successfully", httpRequest);

        // Generate tokens and create session
        return createAuthResponse(user, httpRequest, null, null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findActiveByUsernameOrEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create audit log
        createAuditLog(user, null, "USER_LOGIN", "User", user.getId(),
                "User logged in successfully", httpRequest);

        // Generate tokens and create session
        return createAuthResponse(user, httpRequest, request.getDeviceInfo(), request.getBrowserInfo(), request.getLocation());
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(request.getRefreshToken());
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Invalid token type");
        }

        // Get user from token
        String username = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
        User user = userRepository.findActiveByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if refresh token matches
        if (!request.getRefreshToken().equals(user.getRefreshToken())) {
            throw new RuntimeException("Refresh token mismatch");
        }

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // Update user tokens
        user.setAccessToken(newAccessToken);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInMillis() / 1000)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request, HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(request.getLogoutAllSessions())) {
            // Logout all sessions
            sessionRepository.deactivateAllUserSessions(userId, LocalDateTime.now(), request.getLogoutReason());
            user.setCurrentSessionId(null);
            user.setAccessToken(null);
            user.setRefreshToken(null);
        } else {
            // Logout current session only
            if (user.getCurrentSessionId() != null) {
                sessionRepository.deactivateSession(user.getCurrentSessionId(), LocalDateTime.now(), request.getLogoutReason());
                user.setCurrentSessionId(null);
            }
            user.setAccessToken(null);
            user.setRefreshToken(null);
        }

        userRepository.save(user);

        // Create audit log
        createAuditLog(user, null, "USER_LOGOUT", "User", user.getId(),
                "User logged out", httpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Create audit log
        createAuditLog(user, null, "PASSWORD_CHANGED", "User", user.getId(),
                "User changed password", httpRequest);
    }

    private AuthResponse createAuthResponse(User user, HttpServletRequest httpRequest,
                                           String deviceInfo, String browserInfo, String location) {
        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // Create session
        Session session = new Session();
        session.setUser(user);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setIpAddress(getClientIpAddress(httpRequest));
        session.setUserAgent(httpRequest.getHeader("User-Agent"));
        session.setDeviceInfo(deviceInfo);
        session.setBrowserInfo(browserInfo);
        session.setLocation(location);
        session.setLoginAt(LocalDateTime.now());
        session.setLastActivityAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpirationInMillis() / 1000));
        session.setIsActive(true);

        session = sessionRepository.save(session);

        // Update user with tokens and session
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setCurrentSessionId(session.getId());
        userRepository.save(user);

        // Create audit log for session
        createAuditLog(user, session, "SESSION_CREATED", "Session", session.getId(),
                "New session created", httpRequest);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInMillis() / 1000)
                .user(buildUserResponse(user))
                .session(buildSessionResponse(session))
                .build();
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .isEmailVerified(user.getIsEmailVerified())
                .organisationId(user.getOrganisation() != null ? user.getOrganisation().getId() : null)
                .organisationName(user.getOrganisation() != null ? user.getOrganisation().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private SessionResponse buildSessionResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .sessionToken(session.getSessionToken())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceInfo(session.getDeviceInfo())
                .browserInfo(session.getBrowserInfo())
                .location(session.getLocation())
                .loginAt(session.getLoginAt())
                .lastActivityAt(session.getLastActivityAt())
                .expiresAt(session.getExpiresAt())
                .isActive(session.getIsActive())
                .build();
    }

    private void createAuditLog(User user, Session session, String action, String entityType,
                               Long entityId, String description, HttpServletRequest httpRequest) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setSession(session);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLog.setIpAddress(getClientIpAddress(httpRequest));
        auditLog.setUserAgent(httpRequest.getHeader("User-Agent"));
        auditLog.setRequestMethod(httpRequest.getMethod());
        auditLog.setRequestUrl(httpRequest.getRequestURI());
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

