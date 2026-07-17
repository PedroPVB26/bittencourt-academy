package dev.pedrobittencourt.bittencourt_academy.exception;

import java.time.Instant;

public record StandardError(
    Instant timestamp,
    Integer statusCode,
    String error,
    String message,
    String path
) { }
