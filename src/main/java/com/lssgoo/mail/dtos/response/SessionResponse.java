package com.lssgoo.mail.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private Long userId;
    private String sessionToken;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String browserInfo;
    private String location;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime statusCheckedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private String logoutReason;
    private Integer refreshCount;
    private List<SessionActivityResponse> activities;
}

