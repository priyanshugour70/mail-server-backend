package com.lssgoo.mail.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {

    @NotBlank(message = "To email is required")
    @Email(message = "Invalid to email format")
    private String to;

    private List<@Email String> cc;

    private List<@Email String> bcc;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    @Builder.Default
    private Boolean isHtml = false;

    private List<String> attachments; // File paths or URLs
}

