package dev.pedrobittencourt.email_service.email;

import java.util.Map;

public record EmailMessage(
        String type,
        String destination,
        Map<String, Object> data
) {}