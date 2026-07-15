package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationToken;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationTokenRepository;
import dev.pedrobittencourt.bittencourt_academy.email.EmailService;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyInUseException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyVerifiedException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidTokenException;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

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

    @Test
    @DisplayName("Should verify email successfully")
    void verifyEmailSuccessfully() {
        String token = "valid-token";

        User user = new User();
        user.setEnabled(false);

        EmailVerificationToken tokenEntity = new EmailVerificationToken();
        tokenEntity.setToken(token);
        tokenEntity.setUser(user);
        tokenEntity.setUsed(false);
        tokenEntity.setExpiresAt(Instant.now().plus(48, ChronoUnit.HOURS));

        when(emailVerificationTokenRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        authService.verifiyEmail(token);

        assertAll(
                () -> assertTrue(tokenEntity.isUsed()),
                () -> assertTrue(user.isEnabled())
        );

        verify(emailVerificationTokenRepository).findByToken(token);
        verify(emailVerificationTokenRepository).save(tokenEntity);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token does not exist")
    void verifyEmailWithInvalidToken() {
        String token = "invalid-token";

        when(emailVerificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> authService.verifiyEmail(token)
        );

        verify(emailVerificationTokenRepository).findByToken(token);
        verify(emailVerificationTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ExpiredTokenException when token is expired")
    void verifyEmailWithExpiredToken() {
        String token = "expired-token";

        EmailVerificationToken tokenEntity = new EmailVerificationToken();
        tokenEntity.setToken(token);
        tokenEntity.setUsed(false);
        tokenEntity.setExpiresAt(
                Instant.now().minus(1, ChronoUnit.MINUTES)
        );

        when(emailVerificationTokenRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        assertThrows(
                ExpiredTokenException.class,
                () -> authService.verifiyEmail(token)
        );

        verify(emailVerificationTokenRepository).findByToken(token);
        verify(emailVerificationTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyVerifiedException when email is already verified")
    void verifyEmailAlreadyVerified() {
        String token = "used-token";

        EmailVerificationToken tokenEntity = new EmailVerificationToken();
        tokenEntity.setToken(token);
        tokenEntity.setUsed(true);
        tokenEntity.setExpiresAt(
                Instant.now().plus(48, ChronoUnit.HOURS)
        );

        when(emailVerificationTokenRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        assertThrows(
                EmailAlreadyVerifiedException.class,
                () -> authService.verifiyEmail(token)
        );

        verify(emailVerificationTokenRepository).findByToken(token);

        verify(emailVerificationTokenRepository, never()).save(any());
    }
}