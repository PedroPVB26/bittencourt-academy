package dev.pedrobittencourt.bittencourt_academy.dto;

import java.util.Map;

public record EmailMessage(
        String destination,
        Map<String, Object> data
) {}