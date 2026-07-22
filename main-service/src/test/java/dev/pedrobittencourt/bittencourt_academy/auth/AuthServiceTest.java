package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.AppProperties;
import dev.pedrobittencourt.bittencourt_academy.auth.dto.LoginRequestDto;
import dev.pedrobittencourt.bittencourt_academy.auth.dto.LoginResponseDto;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationToken;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationTokenRepository;
import dev.pedrobittencourt.bittencourt_academy.auth.refreshToken.RefreshToken;
import dev.pedrobittencourt.bittencourt_academy.auth.refreshToken.RefreshTokenService;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.*;
import dev.pedrobittencourt.bittencourt_academy.messaging.EmailPublisher;
import dev.pedrobittencourt.bittencourt_academy.security.JwtService;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private UserService userService;

    @Mock
    private EmailPublisher emailPublisher;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private AppProperties appProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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
        when(appProperties.backendUrl()).thenReturn("http://localhost:8080");


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

        verify(emailPublisher)
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

        when(userService.save(userCreationDto)).thenThrow(new EmailAlreadyInUseException());

        assertThrows(
                EmailAlreadyInUseException.class,
                () -> authService.register(userCreationDto)
        );

        verify(userService).save(userCreationDto);
        verify(emailVerificationTokenRepository, never()).save(any());
        verify(emailPublisher, never()).sendVerificationEmail(any(), any(), any());
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

        authService.verifyEmail(token);

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
                () -> authService.verifyEmail(token)
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
                () -> authService.verifyEmail(token)
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
                () -> authService.verifyEmail(token)
        );

        verify(emailVerificationTokenRepository).findByToken(token);

        verify(emailVerificationTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should resend verification email successfully")
    void resendVerificationEmailSuccessfully() {
        User user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");
        user.setFullName("User Example");

        EmailVerificationToken oldToken = new EmailVerificationToken();
        oldToken.setToken("old-token");
        oldToken.setUser(user);
        oldToken.setUsed(false);
        oldToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(emailVerificationTokenRepository.findByUserEmail(user.getEmail())).thenReturn(Optional.of(oldToken));
        when(appProperties.backendUrl()).thenReturn("http://localhost:8080");

        ArgumentCaptor<EmailVerificationToken> newTokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);

        authService.resendVerificationEmail(user.getEmail());

        verify(emailVerificationTokenRepository).findByUserEmail(user.getEmail());
        verify(emailVerificationTokenRepository).delete(oldToken);
        verify(emailVerificationTokenRepository).save(newTokenCaptor.capture());

        EmailVerificationToken savedNewToken = newTokenCaptor.getValue();
        assertAll(
                () -> assertNotNull(savedNewToken.getToken()),
                () -> assertEquals(user, savedNewToken.getUser()),
                () -> assertNotNull(savedNewToken.getExpiresAt()),
                () -> assertTrue(savedNewToken.getExpiresAt().isAfter(Instant.now()))
        );

        verify(emailPublisher).sendVerificationEmail(
                eq(user.getEmail()),
                contains("token="),
                eq(user.getFullName())
        );
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when there is no token for given email")
    void resendVerificationEmailWithNoToken() {
        String email = "notfound@example.com";

        when(emailVerificationTokenRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> authService.resendVerificationEmail(email));

        verify(emailVerificationTokenRepository).findByUserEmail(email);
        verify(emailVerificationTokenRepository, never()).delete(any());
        verify(emailVerificationTokenRepository, never()).save(any());
        verify(emailPublisher, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyVerifiedException when email is already verified")
    void resendVerificationEmailWhenAlreadyVerified() {
        User user = new User();
        user.setEmail("used@example.com");

        EmailVerificationToken oldToken = new EmailVerificationToken();
        oldToken.setToken("used-token");
        oldToken.setUser(user);
        oldToken.setUsed(true);
        oldToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(emailVerificationTokenRepository.findByUserEmail(user.getEmail())).thenReturn(Optional.of(oldToken));

        assertThrows(EmailAlreadyVerifiedException.class, () -> authService.resendVerificationEmail(user.getEmail()));

        verify(emailVerificationTokenRepository).findByUserEmail(user.getEmail());
        verify(emailVerificationTokenRepository, never()).delete(any());
        verify(emailVerificationTokenRepository, never()).save(any());
        verify(emailPublisher, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void loginSuccessfully() {

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "pedro@gmail.com",
                "MinhaSenha!@123"
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("pedro@gmail.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken("refresh-token");

        when(userService.findEntityByEmail(loginRequestDto.email())).thenReturn(user);

        when(passwordEncoder.matches(
                loginRequestDto.password(),
                user.getPassword()
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user)).thenReturn("access-token");

        when(refreshTokenService.generateRefreshToken(user)).thenReturn(refreshToken);

        LoginResponseDto response = authService.login(loginRequestDto);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("access-token", response.accessToken()),
                () -> assertEquals("refresh-token", response.refreshToken())
        );

        verify(userService).findEntityByEmail(loginRequestDto.email());

        verify(passwordEncoder).matches(
                loginRequestDto.password(),
                user.getPassword()
        );

        verify(jwtService).generateAccessToken(user);

        verify(refreshTokenService).generateRefreshToken(user);
    }

    @Test
    @DisplayName("Should throw DisabledException when user email is not verified")
    void loginWithDisabledUser() {

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "pedro@gmail.com",
                "MinhaSenha!@123"
        );

        User user = new User();
        user.setEmail("pedro@gmail.com");
        user.setPassword("encodedPassword");
        user.setEnabled(false);

        when(userService.findEntityByEmail(loginRequestDto.email())).thenReturn(user);

        when(passwordEncoder.matches(
                loginRequestDto.password(),
                user.getPassword()
        )).thenReturn(true);

        assertThrows(
                DisabledException.class,
                () -> authService.login(loginRequestDto)
        );

        verify(userService).findEntityByEmail(loginRequestDto.email());

        verify(passwordEncoder).matches(
                loginRequestDto.password(),
                user.getPassword()
        );

        verify(jwtService, never()).generateAccessToken(any());

        verify(refreshTokenService, never()).generateRefreshToken(any());
    }

    @Test
    @DisplayName("Should throw exception when user email does not exist")
    void loginWithNonExistingEmail() {

        LoginRequestDto loginRequestDto = new LoginRequestDto(
                "unknown@gmail.com",
                "password"
        );

        when(userService.findEntityByEmail(loginRequestDto.email()))
                .thenThrow(new UserNotFoundException("No user found with email " + loginRequestDto.email()));


        assertThrows(
                UserNotFoundException.class,
                () -> authService.login(loginRequestDto)
        );

        verify(userService).findEntityByEmail(loginRequestDto.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(refreshTokenService);
    }
}