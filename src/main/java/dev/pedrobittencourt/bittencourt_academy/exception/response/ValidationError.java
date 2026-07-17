package dev.pedrobittencourt.bittencourt_academy.exception.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ValidationError(
        Instant timestamp,
        Integer statusCode,
        String error,
        String message,
        String path,
        Map<String, List<String>> errors
) {
}