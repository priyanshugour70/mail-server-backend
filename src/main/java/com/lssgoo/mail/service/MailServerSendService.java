package com.lssgoo.mail.service;

import com.lssgoo.mail.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class MailServerSendService {

    private static final Logger logger = LoggerUtil.getLogger(MailServerSendService.class);

    @Value("${mail.server.host:localhost}")
    private String mailServerHost;

    @Value("${mail.server.port:587}")
    private Integer mailServerPort;

    @Value("${mail.server.username:}")
    private String mailServerUsername;

    @Value("${mail.server.password:}")
    private String mailServerPassword;


    public void sendEmail(String fromEmail, String fromPassword, String to, String subject, String body, boolean isHtml) {
        logger.info("Sending email via internal mail server from: {} to: {}", fromEmail, to);
        
        try {
            JavaMailSender mailSender = getMailSender(fromEmail, fromPassword);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            mailSender.send(message);
            
            logger.info("Email sent successfully from: {} to: {}", fromEmail, to);
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public void sendEmail(String fromEmail, String fromPassword, String to, String[] cc, String[] bcc, 
                         String subject, String body, boolean isHtml) {
        logger.info("Sending email via internal mail server from: {} to: {}", fromEmail, to);
        
        try {
            JavaMailSender mailSender = getMailSender(fromEmail, fromPassword);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            
            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }
            
            if (bcc != null && bcc.length > 0) {
                helper.setBcc(bcc);
            }
            
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            mailSender.send(message);
            
            logger.info("Email sent successfully from: {} to: {}", fromEmail, to);
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
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

