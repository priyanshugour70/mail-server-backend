package com.lssgoo.mail.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionActivityResponse {

    private Long id;
    private Long sessionId;
    private String activityType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime activityTimestamp;
}

