package dev.pedrobittencourt.bittencourt_academy.User.dto;

import dev.pedrobittencourt.bittencourt_academy.User.User;
import dev.pedrobittencourt.bittencourt_academy.User.UserRole;

import java.time.Instant;

public record UserResponseDto(
        Long id,
        String fullName,
        String email,
        UserRole userRole,
        boolean enabled,
        Instant createdAt
) {
    public UserResponseDto(User user) {
        this(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}