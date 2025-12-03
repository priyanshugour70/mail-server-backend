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
import com.lssgoo.mail.entity.SessionActivity;
import com.lssgoo.mail.repository.AuditLogRepository;
import com.lssgoo.mail.repository.OrganisationRepository;
import com.lssgoo.mail.repository.SessionActivityRepository;
import com.lssgoo.mail.repository.SessionRepository;
import com.lssgoo.mail.repository.UserRepository;
import com.lssgoo.mail.security.jwt.JwtTokenProvider;
import com.lssgoo.mail.service.AuthService;
import com.lssgoo.mail.utils.LoggerUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
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
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerUtil.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SessionActivityRepository sessionActivityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        logger.info("Starting user registration for username: {}", request.getUsername());
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed: Username already exists - {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists - {}", request.getEmail());
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

        logger.info("User registered successfully: {} (ID: {})", user.getUsername(), user.getId());
        // Generate tokens and create session
        return createAuthResponse(user, httpRequest, null, null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        logger.info("Starting login process for: {}", request.getUsernameOrEmail());
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("Authentication successful for: {}", request.getUsernameOrEmail());

        // Get user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findActiveByUsernameOrEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found after authentication: {}", userDetails.getUsername());
                    return new RuntimeException("User not found");
                });

        // Create audit log
        createAuditLog(user, null, "USER_LOGIN", "User", user.getId(),
                "User logged in successfully", httpRequest);

        logger.info("Login successful for user: {} (ID: {})", user.getUsername(), user.getId());
        // Generate tokens and create session
        return createAuthResponse(user, httpRequest, request.getDeviceInfo(), request.getBrowserInfo(), request.getLocation());
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Token refresh request received");
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            logger.warn("Token refresh failed: Invalid refresh token");
            throw new RuntimeException("Invalid refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(request.getRefreshToken());
        if (!"refresh".equals(tokenType)) {
            logger.warn("Token refresh failed: Invalid token type - {}", tokenType);
            throw new RuntimeException("Invalid token type");
        }

        // Get user from token
        String username = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
        logger.debug("Token refresh for user: {}", username);
        User user = userRepository.findActiveByUsernameOrEmail(username)
                .orElseThrow(() -> {
                    logger.error("User not found for token refresh: {}", username);
                    return new RuntimeException("User not found");
                });

        // Check if refresh token matches
        if (!request.getRefreshToken().equals(user.getRefreshToken())) {
            logger.warn("Token refresh failed: Refresh token mismatch for user: {}", username);
            throw new RuntimeException("Refresh token mismatch");
        }

        // Get sessionId from the refresh token
        Long sessionId = jwtTokenProvider.getSessionIdFromToken(request.getRefreshToken());
        if (sessionId == null) {
            logger.warn("Token refresh failed: Session ID not found in token");
            throw new RuntimeException("Session ID not found in token");
        }

        // Verify session is still active
        Session session = sessionRepository.findActiveById(sessionId)
                .orElseThrow(() -> {
                    logger.warn("Token refresh failed: Session not found or inactive - Session ID: {}", sessionId);
                    return new RuntimeException("Session not found or inactive");
                });

        // Update session last activity and status
        LocalDateTime now = LocalDateTime.now();
        session.setLastActivityAt(now);
        session.setStatusCheckedAt(now);
        session.setRefreshCount(session.getRefreshCount() != null ? session.getRefreshCount() + 1 : 1);
        sessionRepository.save(session);

        // Create session activity
        createSessionActivity(session, "TOKEN_REFRESHED", "Tokens refreshed successfully", null, null);

        // Generate new tokens with the same sessionId
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), sessionId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), sessionId);

        // Update user tokens
        user.setAccessToken(newAccessToken);
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        // Create audit log
        createAuditLog(user, session, "TOKEN_REFRESHED", "Session", session.getId(),
                "Tokens refreshed successfully", null);

        logger.info("Token refreshed successfully for user: {} (Session ID: {})", username, sessionId);
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

        LocalDateTime now = LocalDateTime.now();
        
        if (Boolean.TRUE.equals(request.getLogoutAllSessions())) {
            // Logout all sessions
            List<Session> activeSessions = sessionRepository.findActiveSessionsByUserId(userId);
            for (Session session : activeSessions) {
                session.setIsActive(false);
                session.setLogoutAt(now);
                session.setLogoutReason(request.getLogoutReason());
                session.setStatusCheckedAt(now);
                sessionRepository.save(session);
                
                // Create session activity
                createSessionActivity(session, "LOGOUT", "User logged out from all sessions", 
                        getClientIpAddress(httpRequest), httpRequest.getHeader("User-Agent"));
            }
            user.setCurrentSessionId(null);
            user.setAccessToken(null);
            user.setRefreshToken(null);
        } else {
            // Logout current session only
            if (user.getCurrentSessionId() != null) {
                Session session = sessionRepository.findActiveById(user.getCurrentSessionId())
                        .orElse(null);
                if (session != null) {
                    session.setIsActive(false);
                    session.setLogoutAt(now);
                    session.setLogoutReason(request.getLogoutReason());
                    session.setStatusCheckedAt(now);
                    sessionRepository.save(session);
                    
                    // Create session activity
                    createSessionActivity(session, "LOGOUT", "User logged out", 
                            getClientIpAddress(httpRequest), httpRequest.getHeader("User-Agent"));
                }
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
        Long sessionId = jwtTokenProvider.getSessionIdFromToken(token);
        
        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify session is active and matches
        if (sessionId != null) {
            Session session = sessionRepository.findActiveById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found or inactive"));
            
            // Update last activity and status
            LocalDateTime now = LocalDateTime.now();
            session.setLastActivityAt(now);
            session.setStatusCheckedAt(now);
            sessionRepository.save(session);
            
            // Create session activity
            createSessionActivity(session, "ACTIVITY_CHECK", "User activity checked", null, null);
        }

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
        // Create session first to get sessionId
        Session session = new Session();
        session.setUser(user);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setIpAddress(getClientIpAddress(httpRequest));
        session.setUserAgent(httpRequest.getHeader("User-Agent"));
        session.setDeviceInfo(deviceInfo);
        session.setBrowserInfo(browserInfo);
        session.setLocation(location);
        LocalDateTime now = LocalDateTime.now();
        session.setLoginAt(now);
        session.setLastActivityAt(now);
        session.setStatusCheckedAt(now);
        session.setRefreshCount(0);
        session.setExpiresAt(now.plusSeconds(jwtTokenProvider.getRefreshTokenExpirationInMillis() / 1000));
        session.setIsActive(true);

        session = sessionRepository.save(session);

        // Create initial session activity
        createSessionActivity(session, "SESSION_CREATED", "Session created on login", 
                getClientIpAddress(httpRequest), httpRequest.getHeader("User-Agent"));

        // Generate tokens with sessionId
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), session.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), session.getId());

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
                .statusCheckedAt(session.getStatusCheckedAt())
                .expiresAt(session.getExpiresAt())
                .isActive(session.getIsActive())
                .refreshCount(session.getRefreshCount() != null ? session.getRefreshCount() : 0)
                .build();
    }

    private void createSessionActivity(Session session, String activityType, String description, 
                                      String ipAddress, String userAgent) {
        SessionActivity activity = new SessionActivity();
        activity.setSession(session);
        activity.setActivityType(activityType);
        activity.setDescription(description);
        activity.setIpAddress(ipAddress);
        activity.setUserAgent(userAgent);
        activity.setActivityTimestamp(LocalDateTime.now());
        sessionActivityRepository.save(activity);
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
        if (httpRequest != null) {
            auditLog.setIpAddress(getClientIpAddress(httpRequest));
            auditLog.setUserAgent(httpRequest.getHeader("User-Agent"));
            auditLog.setRequestMethod(httpRequest.getMethod());
            auditLog.setRequestUrl(httpRequest.getRequestURI());
        }
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

