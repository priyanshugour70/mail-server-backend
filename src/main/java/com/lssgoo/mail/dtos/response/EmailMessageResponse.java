package com.lssgoo.mail.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageResponse {

    private Long messageId; // IMAP message number
    private String messageUid; // IMAP UID
    private String from; // Single from address
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String body;
    private Boolean isHtml;
    private Date sentDate;
    private Date receivedDate;
    private Boolean isRead;
    private Boolean hasAttachments;
    private List<EmailAttachmentResponse> attachments;
    private String folder; // INBOX, SENT, etc.
}

