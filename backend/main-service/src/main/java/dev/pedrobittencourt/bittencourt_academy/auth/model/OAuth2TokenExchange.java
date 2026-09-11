package dev.pedrobittencourt.bittencourt_academy.auth.model;

import java.time.Instant;

public record OAuth2TokenExchange(
        Long userId,
        Instant expiresAt
) {}