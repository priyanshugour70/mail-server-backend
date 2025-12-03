package com.lssgoo.mail.dtos;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    private String message;
    private String code;
    private Map<String, String> details;
    private LocalDateTime timestamp;
}
