package com.lssgoo.mail;

import com.lssgoo.mail.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailServerBackendApplication {

	private static final Logger logger = LoggerUtil.getLogger(MailServerBackendApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Mail Server Backend Application...");
		SpringApplication.run(MailServerBackendApplication.class, args);
		logger.info("Mail Server Backend Application started successfully");
	}

}
