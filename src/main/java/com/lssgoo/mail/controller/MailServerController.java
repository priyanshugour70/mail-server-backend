package com.lssgoo.mail.controller;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.dtos.request.ReplyEmailRequest;
import com.lssgoo.mail.dtos.request.SendEmailRequest;
import com.lssgoo.mail.dtos.response.EmailMessageResponse;
import com.lssgoo.mail.service.MailDnsService;
import com.lssgoo.mail.service.MailReceiveService;
import com.lssgoo.mail.service.MailReplyService;
import com.lssgoo.mail.service.MailServerSendService;
import com.lssgoo.mail.service.MailServerUserService;
import com.lssgoo.mail.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mail/server")
@Tag(name = "Mail Server", description = "Internal mail server management APIs")
@SecurityRequirement(name = "bearerAuth")
public class MailServerController {

    private static final Logger logger = LoggerUtil.getLogger(MailServerController.class);

    @Autowired
    private MailServerSendService mailServerSendService;

    @Autowired
    private MailServerUserService mailServerUserService;

    @Autowired
    private MailDnsService mailDnsService;

    @Autowired
    private MailReceiveService mailReceiveService;

    @Autowired
    private MailReplyService mailReplyService;

    @Operation(summary = "Send email via internal mail server", description = "Sends an email using the internal docker-mailserver. Requires email and password in request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/send")
    public ResponseEntity<APIResponse<Void>> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest httpRequest) {
        logger.info("Send email via internal server request received - From: {} To: {}", email, request.getTo());
        try {
            String[] cc = request.getCc() != null ? request.getCc().toArray(new String[0]) : null;
            String[] bcc = request.getBcc() != null ? request.getBcc().toArray(new String[0]) : null;
            
            mailServerSendService.sendEmail(
                    email,
                    password,
                    request.getTo(),
                    cc,
                    bcc,
                    request.getSubject(),
                    request.getBody(),
                    request.getIsHtml() != null ? request.getIsHtml() : false
            );
            logger.info("Email sent successfully via internal server from: {} to: {}", email, request.getTo());
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Email sent successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to send email via internal server - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get inbox messages", description = "Retrieves messages from the inbox folder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/inbox")
    public ResponseEntity<APIResponse<List<EmailMessageResponse>>> getInbox(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        logger.info("Get inbox request received for: {} (limit: {}, offset: {})", email, limit, offset);
        try {
            List<EmailMessageResponse> messages = mailReceiveService.getInboxMessages(email, password, limit, offset);
            logger.info("Retrieved {} messages from inbox for: {}", messages.size(), email);
            return ResponseEntity.ok(APIResponse.<List<EmailMessageResponse>>builder()
                    .success(true)
                    .message("Messages retrieved successfully")
                    .data(messages)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get inbox messages - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<List<EmailMessageResponse>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get email message", description = "Retrieves a specific email message by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/message/{messageId}")
    public ResponseEntity<APIResponse<EmailMessageResponse>> getMessage(
            @PathVariable Long messageId,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "INBOX") String folder) {
        logger.info("Get message request received - ID: {}, Folder: {}, Email: {}", messageId, folder, email);
        try {
            EmailMessageResponse message = mailReceiveService.getMessage(email, password, messageId, folder);
            logger.info("Message retrieved successfully: {}", messageId);
            return ResponseEntity.ok(APIResponse.<EmailMessageResponse>builder()
                    .success(true)
                    .message("Message retrieved successfully")
                    .data(message)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get message - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<EmailMessageResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Mark message as read", description = "Marks an email message as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message marked as read",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/message/{messageId}/read")
    public ResponseEntity<APIResponse<Void>> markAsRead(
            @PathVariable Long messageId,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "INBOX") String folder) {
        logger.info("Mark as read request received - ID: {}, Folder: {}, Email: {}", messageId, folder, email);
        try {
            mailReceiveService.markAsRead(email, password, messageId, folder);
            logger.info("Message marked as read: {}", messageId);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Message marked as read")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to mark message as read - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Delete email message", description = "Deletes an email message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message deleted successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<APIResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "INBOX") String folder) {
        logger.info("Delete message request received - ID: {}, Folder: {}, Email: {}", messageId, folder, email);
        try {
            mailReceiveService.deleteMessage(email, password, messageId, folder);
            logger.info("Message deleted successfully: {}", messageId);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Message deleted successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to delete message - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Reply to email", description = "Replies to an email message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reply sent successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/reply")
    public ResponseEntity<APIResponse<Void>> replyToEmail(
            @Valid @RequestBody ReplyEmailRequest request,
            @RequestParam String email,
            @RequestParam String password) {
        logger.info("Reply to email request received - Message ID: {}, From: {}", request.getMessageId(), email);
        try {
            mailReplyService.replyToMessage(email, password, request);
            logger.info("Reply sent successfully for message: {}", request.getMessageId());
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Reply sent successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to send reply - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Create mailbox user", description = "Creates a new mailbox user in docker-mailserver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mailbox created successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/users/create")
    public ResponseEntity<APIResponse<Void>> createUser(
            @RequestParam String email,
            @RequestParam String password) {
        logger.info("Create mailbox user request received: {}", email);
        try {
            mailServerUserService.createMailbox(email, password);
            logger.info("Mailbox user created successfully: {}", email);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Mailbox user created successfully")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to create mailbox user: {} - Error: {}", email, e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get DNS records", description = "Retrieves SPF, DKIM, and DMARC DNS records for the mail server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DNS records retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/dns")
    public ResponseEntity<APIResponse<Map<String, String>>> getDns() {
        logger.info("Get DNS records request received");
        try {
            Map<String, String> dnsRecords = mailDnsService.getDnsRecords();
            logger.info("DNS records retrieved successfully");
            return ResponseEntity.ok(APIResponse.<Map<String, String>>builder()
                    .success(true)
                    .message("DNS records retrieved successfully")
                    .data(dnsRecords)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get DNS records - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Map<String, String>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get mail server health", description = "Checks the health status of the mail server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health check completed",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/health")
    public ResponseEntity<APIResponse<Map<String, Object>>> getHealth() {
        logger.info("Mail server health check request received");
        try {
            Map<String, Object> health = mailDnsService.getDnsStatus();
            health.put("status", "healthy");
            logger.info("Mail server health check completed");
            return ResponseEntity.ok(APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Mail server is healthy")
                    .data(health)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Mail server health check failed - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Restart mail server", description = "Restarts the docker-mailserver container")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mail server restart initiated",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @PostMapping("/restart")
    public ResponseEntity<APIResponse<Void>> restartMailServer() {
        logger.info("Restart mail server request received");
        try {
            mailServerUserService.restartMailServer();
            logger.info("Mail server restart initiated successfully");
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(true)
                    .message("Mail server restart initiated")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to restart mail server - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}

