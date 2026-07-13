package dev.pedrobittencourt.bittencourt_academy.User.dto;

import dev.pedrobittencourt.bittencourt_academy.User.UserRole;
import jakarta.validation.constraints.*;


public record UserCreationDto(
        @NotBlank(message = "name must not be blank")
        @Size(
                min = 3,
                max = 120,
                message = "name must contain between 3 and 120 characters"
        )
        String fullName,

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        @Size(
                max = 150,
                message = "email must contain at most 150 characters"
        )
        String email,

        @NotBlank(message = "password must not be blank")
        @Size(
                min = 8,
                max = 100,
                message = "password must contain between 8 and 100 characters"
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "password must contain at least one uppercase letter, one lowercase letter and one number"
        )
        String password,

        @NotNull(message = "userRole must not be null")
        UserRole userRole
) {
}
