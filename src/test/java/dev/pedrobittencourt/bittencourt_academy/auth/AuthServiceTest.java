package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationToken;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationTokenRepository;
import dev.pedrobittencourt.bittencourt_academy.email.EmailService;
import dev.pedrobittencourt.bittencourt_academy.exception.EmailAlreadyInUseException;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import dev.pedrobittencourt.bittencourt_academy.user.UserService;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")

class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserService  userService;

    @Mock
    private EmailService  emailService;

    @Mock
    private EmailVerificationTokenRepository  emailVerificationTokenRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this);}

    @Test
    @DisplayName("Should successfully register an user")
    void register() {
        UserCreationDto userCreationDto = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "MinhaSenha!@123"
        );

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName(userCreationDto.fullName());
        savedUser.setEmail(userCreationDto.email());

        when(userService.save(userCreationDto)).thenReturn(savedUser);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

        UserResponseDto response = authService.register(userCreationDto);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(savedUser.getId(), response.id()),
                () -> assertEquals(savedUser.getFullName(), response.fullName()),
                () -> assertEquals(savedUser.getEmail(), response.email())
        );

        verify(userService).save(userCreationDto);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();

        assertAll(
                () -> assertNotNull(savedToken.getToken()),
                () -> assertEquals(savedUser, savedToken.getUser()),
                () -> assertNotNull(savedToken.getExpiresAt()),
                () -> assertTrue(savedToken.getExpiresAt().isAfter(Instant.now()))
        );

        verify(emailService)
                .sendVerificationEmail(
                        eq(savedUser.getEmail()),
                        contains("token="),
                        eq(savedUser.getFullName())
                );
    }

    @Test
    @DisplayName("Should not register user when email is already in use")
    void registerWithUsedEmail() {
        UserCreationDto userCreationDto = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "MinhaSenha!@123"
        );

        when(userService.save(userCreationDto)).thenThrow(new EmailAlreadyInUseException("Email already in use"));

        assertThrows(
                EmailAlreadyInUseException.class,
                () -> authService.register(userCreationDto)
        );

        verify(userService).save(userCreationDto);

        verify(emailVerificationTokenRepository, never()).save(any());

        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }
}