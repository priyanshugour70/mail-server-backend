package com.lssgoo.mail.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyEmailRequest {

    @NotNull(message = "Message ID is required")
    private Long messageId;

    @NotBlank(message = "Reply body is required")
    private String body;

    @Builder.Default
    private Boolean isHtml = false;

    @Builder.Default
    private Boolean replyAll = false; // Reply to all recipients if true
}

