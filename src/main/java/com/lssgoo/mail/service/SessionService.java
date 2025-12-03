package com.lssgoo.mail.service;

import com.lssgoo.mail.dtos.response.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SessionService {

    SessionResponse getCurrentSession(HttpServletRequest httpRequest);

    SessionResponse getSessionById(Long sessionId, HttpServletRequest httpRequest);

    List<SessionResponse> getAllUserSessions(HttpServletRequest httpRequest);

    List<SessionResponse> getActiveUserSessions(HttpServletRequest httpRequest);

    void updateSessionStatus(HttpServletRequest httpRequest);
}

