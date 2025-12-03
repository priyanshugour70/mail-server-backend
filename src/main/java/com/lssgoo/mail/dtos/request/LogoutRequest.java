package com.lssgoo.mail.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    private String logoutReason;
    private Boolean logoutAllSessions = false;
}

