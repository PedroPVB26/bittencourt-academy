package dev.pedrobittencourt.bittencourt_academy.auth.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {}
