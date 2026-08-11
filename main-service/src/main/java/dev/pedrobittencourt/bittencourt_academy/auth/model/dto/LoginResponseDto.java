package dev.pedrobittencourt.bittencourt_academy.auth.model.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {}
