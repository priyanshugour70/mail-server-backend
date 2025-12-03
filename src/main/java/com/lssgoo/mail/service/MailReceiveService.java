package com.lssgoo.mail.service;

import com.lssgoo.mail.dtos.response.EmailAttachmentResponse;
import com.lssgoo.mail.dtos.response.EmailMessageResponse;
import com.lssgoo.mail.utils.LoggerUtil;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class MailReceiveService {

    private static final Logger logger = LoggerUtil.getLogger(MailReceiveService.class);

    @Value("${mail.server.imap.host:localhost}")
    private String imapHost;

    @Value("${mail.server.imap.port:993}")
    private Integer imapPort;

    @Value("${mail.server.imap.ssl:true}")
    private Boolean imapSsl;

    @Value("${mail.server.username:}")
    private String mailServerUsername;

    @Value("${mail.server.password:}")
    private String mailServerPassword;


    public List<EmailMessageResponse> getInboxMessages(String email, String password, int limit, int offset) {
        logger.info("Fetching inbox messages for: {} (limit: {}, offset: {})", email, limit, offset);
        
        List<EmailMessageResponse> messages = new ArrayList<>();
        
        try {
            Properties props = new Properties();
            props.put("mail.imap.host", imapHost);
            props.put("mail.imap.port", imapPort);
            props.put("mail.imap.auth", "true");

            if (imapSsl) {
                props.put("mail.imap.ssl.enable", "true");
                props.put("mail.imap.ssl.trust", imapHost);
            }

            Session session = Session.getInstance(props);
            Store store = session.getStore(imapSsl ? "imaps" : "imap");
            store.connect(imapHost, imapPort, email, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int totalMessages = inbox.getMessageCount();
            int start = Math.max(1, totalMessages - offset - limit + 1);
            int end = totalMessages - offset;

            if (start <= end && start > 0) {
                Message[] msgs = inbox.getMessages(start, end);
                
                for (int i = msgs.length - 1; i >= 0; i--) {
                    messages.add(convertToResponse(msgs[i], "INBOX"));
                }
            }

            inbox.close(false);
            store.close();
            
            logger.info("Retrieved {} messages from inbox for: {}", messages.size(), email);
        } catch (Exception e) {
            logger.error("Failed to fetch inbox messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch inbox messages: " + e.getMessage(), e);
        }

        return messages;
    }

    public EmailMessageResponse getMessage(String email, String password, Long messageId, String folder) {
        logger.info("Fetching message {} from folder {} for: {}", messageId, folder, email);
        
        try {
            Properties props = new Properties();
            props.put("mail.imap.host", imapHost);
            props.put("mail.imap.port", imapPort);
            props.put("mail.imap.auth", "true");

            if (imapSsl) {
                props.put("mail.imap.ssl.enable", "true");
                props.put("mail.imap.ssl.trust", imapHost);
            }

            Session session = Session.getInstance(props);
            Store store = session.getStore(imapSsl ? "imaps" : "imap");
            store.connect(imapHost, imapPort, email, password);

            Folder emailFolder = store.getFolder(folder != null ? folder : "INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message message = emailFolder.getMessage(messageId.intValue());
            EmailMessageResponse response = convertToResponse(message, folder != null ? folder : "INBOX");

            emailFolder.close(false);
            store.close();
            
            logger.info("Message retrieved successfully: {}", messageId);
            return response;
        } catch (Exception e) {
            logger.error("Failed to fetch message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch message: " + e.getMessage(), e);
        }
    }

    public void markAsRead(String email, String password, Long messageId, String folder) {
        logger.info("Marking message {} as read in folder {} for: {}", messageId, folder, email);
        
        try {
            Properties props = new Properties();
            props.put("mail.imap.host", imapHost);
            props.put("mail.imap.port", imapPort);
            props.put("mail.imap.auth", "true");

            if (imapSsl) {
                props.put("mail.imap.ssl.enable", "true");
                props.put("mail.imap.ssl.trust", imapHost);
            }

            Session session = Session.getInstance(props);
            Store store = session.getStore(imapSsl ? "imaps" : "imap");
            store.connect(imapHost, imapPort, email, password);

            Folder emailFolder = store.getFolder(folder != null ? folder : "INBOX");
            emailFolder.open(Folder.READ_WRITE);

            Message message = emailFolder.getMessage(messageId.intValue());
            message.setFlag(Flags.Flag.SEEN, true);

            emailFolder.close(true);
            store.close();
            
            logger.info("Message marked as read: {}", messageId);
        } catch (Exception e) {
            logger.error("Failed to mark message as read: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark message as read: " + e.getMessage(), e);
        }
    }

    public void deleteMessage(String email, String password, Long messageId, String folder) {
        logger.info("Deleting message {} from folder {} for: {}", messageId, folder, email);
        
        try {
            Properties props = new Properties();
            props.put("mail.imap.host", imapHost);
            props.put("mail.imap.port", imapPort);
            props.put("mail.imap.auth", "true");

            if (imapSsl) {
                props.put("mail.imap.ssl.enable", "true");
                props.put("mail.imap.ssl.trust", imapHost);
            }

            Session session = Session.getInstance(props);
            Store store = session.getStore(imapSsl ? "imaps" : "imap");
            store.connect(imapHost, imapPort, email, password);

            Folder emailFolder = store.getFolder(folder != null ? folder : "INBOX");
            emailFolder.open(Folder.READ_WRITE);

            Message message = emailFolder.getMessage(messageId.intValue());
            message.setFlag(Flags.Flag.DELETED, true);

            emailFolder.close(true);
            store.close();
            
            logger.info("Message deleted: {}", messageId);
        } catch (Exception e) {
            logger.error("Failed to delete message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete message: " + e.getMessage(), e);
        }
    }

    private EmailMessageResponse convertToResponse(Message message, String folder) throws Exception {
        EmailMessageResponse.EmailMessageResponseBuilder builder = EmailMessageResponse.builder();

        builder.messageId((long) message.getMessageNumber());
        // Use message number as UID if IMAP UID is not available
        try {
            if (message instanceof MimeMessage) {
                // Try to get UID using reflection or use message number
                builder.messageUid(String.valueOf(message.getMessageNumber()));
            } else {
                builder.messageUid(String.valueOf(message.getMessageNumber()));
            }
        } catch (Exception e) {
            builder.messageUid(String.valueOf(message.getMessageNumber()));
        }
        List<String> fromList = getAddresses(message.getFrom());
        builder.from(fromList.isEmpty() ? null : fromList.get(0));
        builder.to(getAddresses(message.getRecipients(Message.RecipientType.TO)));
        builder.cc(getAddresses(message.getRecipients(Message.RecipientType.CC)));
        builder.bcc(getAddresses(message.getRecipients(Message.RecipientType.BCC)));
        builder.subject(message.getSubject());
        builder.sentDate(message.getSentDate());
        builder.receivedDate(message.getReceivedDate());
        builder.isRead(message.isSet(Flags.Flag.SEEN));
        builder.folder(folder);

        // Extract body and attachments
        Object content = message.getContent();
        boolean isHtml = false;
        if (content instanceof String) {
            builder.body((String) content);
            builder.isHtml(false);
            builder.hasAttachments(false);
        } else if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            List<EmailAttachmentResponse> attachments = new ArrayList<>();
            StringBuilder bodyText = new StringBuilder();
            StringBuilder bodyHtml = new StringBuilder();

            for (int i = 0; i < multipart.getCount(); i++) {
                jakarta.mail.internet.MimeBodyPart part = (jakarta.mail.internet.MimeBodyPart) multipart.getBodyPart(i);
                
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    EmailAttachmentResponse attachment = EmailAttachmentResponse.builder()
                            .filename(part.getFileName())
                            .contentType(part.getContentType())
                            .size((long) part.getSize())
                            .build();
                    attachments.add(attachment);
                } else {
                    String contentType = part.getContentType();
                    if (contentType != null && contentType.toLowerCase().contains("text/html")) {
                        bodyHtml.append(part.getContent());
                        isHtml = true;
                    } else if (contentType != null && contentType.toLowerCase().contains("text/plain")) {
                        bodyText.append(part.getContent());
                    }
                }
            }

            builder.body(isHtml ? bodyHtml.toString() : bodyText.toString());
            builder.isHtml(isHtml);
            builder.hasAttachments(!attachments.isEmpty());
            builder.attachments(attachments);
        }

        return builder.build();
    }

    private List<String> getAddresses(Address[] addresses) {
        if (addresses == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (Address address : addresses) {
            if (address instanceof InternetAddress) {
                result.add(((InternetAddress) address).getAddress());
            } else {
                result.add(address.toString());
            }
        }
        return result;
    }
}

