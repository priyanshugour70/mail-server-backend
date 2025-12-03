package com.lssgoo.mail.service.impl;

import com.lssgoo.mail.dtos.response.SessionActivityResponse;
import com.lssgoo.mail.dtos.response.SessionResponse;
import com.lssgoo.mail.entity.Session;
import com.lssgoo.mail.entity.SessionActivity;
import com.lssgoo.mail.repository.SessionActivityRepository;
import com.lssgoo.mail.repository.SessionRepository;
import com.lssgoo.mail.security.jwt.JwtTokenProvider;
import com.lssgoo.mail.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionActivityRepository sessionActivityRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getCurrentSession(HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Long sessionId = jwtTokenProvider.getSessionIdFromToken(token);

        if (sessionId == null) {
            throw new RuntimeException("Session ID not found in token");
        }

        Session session = sessionRepository.findActiveById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        // Verify session belongs to user
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Session does not belong to user");
        }

        // Update status
        updateSessionStatus(session);

        return buildSessionResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long sessionId, HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Verify session belongs to user
        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Session does not belong to user");
        }

        // Update status if active
        if (Boolean.TRUE.equals(session.getIsActive())) {
            updateSessionStatus(session);
        }

        return buildSessionResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getAllUserSessions(HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<Session> sessions = sessionRepository.findAllSessionsByUserId(userId);
        return sessions.stream()
                .map(this::buildSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getActiveUserSessions(HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        List<Session> sessions = sessionRepository.findActiveSessionsByUserId(userId);
        return sessions.stream()
                .map(session -> {
                    updateSessionStatus(session);
                    return buildSessionResponse(session);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateSessionStatus(HttpServletRequest httpRequest) {
        String token = extractTokenFromRequest(httpRequest);
        if (token == null) {
            throw new RuntimeException("Token not found");
        }

        Long sessionId = jwtTokenProvider.getSessionIdFromToken(token);
        if (sessionId == null) {
            throw new RuntimeException("Session ID not found in token");
        }

        Session session = sessionRepository.findActiveById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        updateSessionStatus(session);
    }

    private void updateSessionStatus(Session session) {
        LocalDateTime now = LocalDateTime.now();
        session.setLastActivityAt(now);
        session.setStatusCheckedAt(now);
        sessionRepository.save(session);

        // Create session activity
        createSessionActivity(session, "STATUS_CHECK", "Session status checked", null, null);
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

    private SessionResponse buildSessionResponse(Session session) {
        List<SessionActivity> activities = sessionActivityRepository.findBySessionId(session.getId());
        List<SessionActivityResponse> activityResponses = activities.stream()
                .map(this::buildActivityResponse)
                .collect(Collectors.toList());

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
                .logoutAt(session.getLogoutAt())
                .lastActivityAt(session.getLastActivityAt())
                .statusCheckedAt(session.getStatusCheckedAt())
                .expiresAt(session.getExpiresAt())
                .isActive(session.getIsActive())
                .logoutReason(session.getLogoutReason())
                .refreshCount(session.getRefreshCount() != null ? session.getRefreshCount() : 0)
                .activities(activityResponses)
                .build();
    }

    private SessionActivityResponse buildActivityResponse(SessionActivity activity) {
        return SessionActivityResponse.builder()
                .id(activity.getId())
                .sessionId(activity.getSession().getId())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .ipAddress(activity.getIpAddress())
                .userAgent(activity.getUserAgent())
                .activityTimestamp(activity.getActivityTimestamp())
                .build();
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

