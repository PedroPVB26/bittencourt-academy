package dev.pedrobittencourt.bittencourt_academy;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String backendUrl, String frontendUrl
) {}
