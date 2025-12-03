package com.lssgoo.mail.config;

import com.lssgoo.mail.utils.LoggerUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final Logger logger = LoggerUtil.getLogger(SwaggerConfig.class);

    @Bean
    public OpenAPI customOpenAPI() {
        logger.info("Initializing Swagger/OpenAPI configuration");
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Mail Server Backend API")
                        .version("1.0.0")
                        .description("A backend REST API that manages an external mail server, creates mail users, sends email using SMTP, reads inbox via IMAP, and prints DNS records (SPF, DKIM, DMARC)")
                        .contact(new Contact()
                                .name("Mail Server Team")
                                .email("support@mailserver.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication. Enter your JWT token in the format: Bearer {token}")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        logger.info("Configuring Swagger API group: mail-server-api");
        return GroupedOpenApi.builder()
                .group("mail-server-api")
                .pathsToMatch("/api/**")
                .packagesToExclude("com.lssgoo.mail.exceptions")
                .build();
    }
}

