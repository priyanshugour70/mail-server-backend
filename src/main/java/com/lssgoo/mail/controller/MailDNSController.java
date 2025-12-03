package com.lssgoo.mail.controller;

import com.lssgoo.mail.dtos.APIResponse;
import com.lssgoo.mail.service.MailDnsService;
import com.lssgoo.mail.utils.LoggerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mail/dns")
@Tag(name = "Mail DNS", description = "DNS configuration and status APIs")
@SecurityRequirement(name = "bearerAuth")
public class MailDNSController {

    private static final Logger logger = LoggerUtil.getLogger(MailDNSController.class);

    @Autowired
    private MailDnsService mailDnsService;

    @Operation(summary = "Get DNS server records", description = "Retrieves all DNS records (SPF, DKIM, DMARC, MX) for the mail server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DNS records retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/server")
    public ResponseEntity<APIResponse<Map<String, String>>> getDnsServer() {
        logger.info("Get DNS server records request received");
        try {
            Map<String, String> dnsRecords = mailDnsService.getDnsRecords();
            logger.info("DNS server records retrieved successfully");
            return ResponseEntity.ok(APIResponse.<Map<String, String>>builder()
                    .success(true)
                    .message("DNS records retrieved successfully")
                    .data(dnsRecords)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get DNS server records - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Map<String, String>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @Operation(summary = "Get DNS status", description = "Retrieves DNS configuration status and information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DNS status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<APIResponse<Map<String, Object>>> getDnsStatus() {
        logger.info("Get DNS status request received");
        try {
            Map<String, Object> status = mailDnsService.getDnsStatus();
            logger.info("DNS status retrieved successfully");
            return ResponseEntity.ok(APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("DNS status retrieved successfully")
                    .data(status)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            logger.error("Failed to get DNS status - Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }
}

