package com.lssgoo.mail.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentResponse {

    private String filename;
    private String contentType;
    private Long size;
    private String contentId; // For inline attachments
}

