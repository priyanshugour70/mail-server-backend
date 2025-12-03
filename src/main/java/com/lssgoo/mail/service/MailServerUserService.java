package com.lssgoo.mail.service;

import com.lssgoo.mail.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MailServerUserService {

    private static final Logger logger = LoggerUtil.getLogger(MailServerUserService.class);

    @Value("${mail.server.scripts.path:./scripts}")
    private String scriptsPath;

    public void createMailbox(String email, String password) {
        logger.info("Creating mailbox for: {}", email);
        executeScript("add-user.sh", email, password);
    }

    public void deleteMailbox(String email) {
        logger.info("Deleting mailbox for: {}", email);
        executeScript("delete-user.sh", email);
    }

    public String listUsers() {
        logger.info("Listing mail users");
        return executeScript("list-users.sh");
    }

    public String getDkimKey() {
        logger.info("Getting DKIM key");
        return executeScript("get-dkim.sh");
    }

    public void restartMailServer() {
        logger.info("Restarting mail server");
        executeScript("restart-mail.sh");
    }

    private void executeScript(String scriptName, String... args) {
        try {
            Path scriptPath = Paths.get(scriptsPath, scriptName);
            if (!Files.exists(scriptPath)) {
                logger.error("Script not found: {}", scriptPath);
                throw new RuntimeException("Script not found: " + scriptName);
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", scriptPath.toString());
            if (args.length > 0) {
                for (String arg : args) {
                    processBuilder.command().add(arg);
                }
            }
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append("\n");
                }
                logger.error("Script execution failed: {} - Exit code: {} - Error: {}", scriptName, exitCode, error);
                throw new RuntimeException("Script execution failed: " + error);
            }

            logger.info("Script executed successfully: {}", scriptName);
        } catch (Exception e) {
            logger.error("Error executing script {}: {}", scriptName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute script: " + e.getMessage(), e);
        }
    }

    private String executeScript(String scriptName) {
        try {
            Path scriptPath = Paths.get(scriptsPath, scriptName);
            if (!Files.exists(scriptPath)) {
                logger.error("Script not found: {}", scriptPath);
                throw new RuntimeException("Script not found: " + scriptName);
            }

            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath.toString());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Script execution failed: {} - Exit code: {} - Output: {}", scriptName, exitCode, output);
                throw new RuntimeException("Script execution failed: " + output);
            }

            logger.info("Script executed successfully: {}", scriptName);
            return output.toString();
        } catch (Exception e) {
            logger.error("Error executing script {}: {}", scriptName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute script: " + e.getMessage(), e);
        }
    }
}

