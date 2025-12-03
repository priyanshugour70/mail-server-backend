package com.lssgoo.mail.service;

import com.lssgoo.mail.dtos.request.ReplyEmailRequest;
import com.lssgoo.mail.dtos.response.EmailMessageResponse;
import com.lssgoo.mail.utils.LoggerUtil;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class MailReplyService {

    private static final Logger logger = LoggerUtil.getLogger(MailReplyService.class);

    @Autowired
    private MailReceiveService mailReceiveService;

    @Value("${mail.server.host:localhost}")
    private String mailServerHost;

    @Value("${mail.server.port:587}")
    private Integer mailServerPort;

    @Value("${mail.server.username:}")
    private String mailServerUsername;

    @Value("${mail.server.password:}")
    private String mailServerPassword;

    public void replyToMessage(String email, String password, ReplyEmailRequest request) {
        logger.info("Replying to message {} for: {}", request.getMessageId(), email);
        
        try {
            // First, get the original message
            EmailMessageResponse originalMessage = mailReceiveService.getMessage(
                    email, password, request.getMessageId(), "INBOX");

            // Create reply message
            JavaMailSender mailSender = getMailSender(email, password);
            MimeMessage replyMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(replyMessage, true, "UTF-8");

            // Set reply headers
            if (originalMessage.getFrom() != null && !originalMessage.getFrom().isEmpty()) {
                helper.setTo(originalMessage.getFrom());
            }

            // If replyAll, include CC recipients
            if (Boolean.TRUE.equals(request.getReplyAll())) {
                if (originalMessage.getCc() != null && !originalMessage.getCc().isEmpty()) {
                    helper.setCc(originalMessage.getCc().toArray(new String[0]));
                }
            }

            // Set subject with Re: prefix if not already present
            String subject = originalMessage.getSubject();
            if (subject != null && !subject.startsWith("Re: ")) {
                subject = "Re: " + subject;
            }
            helper.setSubject(subject);

            // Set from
            helper.setFrom(email);

            // Build reply body
            StringBuilder replyBody = new StringBuilder();
            if (request.getIsHtml() != null && request.getIsHtml()) {
                replyBody.append("<div>");
                replyBody.append(request.getBody());
                replyBody.append("</div>");
                replyBody.append("<br><br>");
                replyBody.append("<div style='border-left: 3px solid #ccc; padding-left: 10px; margin-left: 10px;'>");
                replyBody.append("<p><strong>From:</strong> ").append(originalMessage.getFrom()).append("</p>");
                replyBody.append("<p><strong>Date:</strong> ").append(originalMessage.getSentDate()).append("</p>");
                replyBody.append("<p><strong>Subject:</strong> ").append(originalMessage.getSubject()).append("</p>");
                replyBody.append("<hr>");
                replyBody.append(originalMessage.getBody());
                replyBody.append("</div>");
                helper.setText(replyBody.toString(), true);
            } else {
                replyBody.append(request.getBody());
                replyBody.append("\n\n");
                replyBody.append("----- Original Message -----\n");
                replyBody.append("From: ").append(originalMessage.getFrom()).append("\n");
                replyBody.append("Date: ").append(originalMessage.getSentDate()).append("\n");
                replyBody.append("Subject: ").append(originalMessage.getSubject()).append("\n\n");
                replyBody.append(originalMessage.getBody());
                helper.setText(replyBody.toString(), false);
            }

            // Send the reply
            mailSender.send(replyMessage);
            logger.info("Reply sent successfully for message: {}", request.getMessageId());
        } catch (Exception e) {
            logger.error("Failed to send reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send reply: " + e.getMessage(), e);
        }
    }

    private JavaMailSender getMailSender(String email, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailServerHost);
        mailSender.setPort(mailServerPort);
        mailSender.setUsername(email);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");

        return mailSender;
    }
}

