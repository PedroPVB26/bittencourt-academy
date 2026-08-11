package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.auth.model.dto.LoginRequestDto;
import dev.pedrobittencourt.bittencourt_academy.auth.model.dto.LoginResponseDto;
import dev.pedrobittencourt.bittencourt_academy.auth.oauth2.GoogleOAuth2UserService;
import dev.pedrobittencourt.bittencourt_academy.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.*;
import dev.pedrobittencourt.bittencourt_academy.security.JwtAuthenticationFilter;
import dev.pedrobittencourt.bittencourt_academy.security.SecurityConfig;
import dev.pedrobittencourt.bittencourt_academy.user.UserRole;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockitoBean
    private GoogleOAuth2UserService googleOAuth2UserService;

    @BeforeEach
    void setUp() throws Exception {
        // Ensina o mock do filtro a passar a requisição adiante na corrente
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Should return 200 OK when login credentials are valid")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDto request = new LoginRequestDto(
                "pedro@gmail.com",
                "Senha123!"
        );

        LoginResponseDto response = new LoginResponseDto(
                "access-token",
                "refresh-token"
        );

        when(authService.localLogin(request)).thenReturn(response);

        mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).localLogin(request);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when credentials are invalid")
    void shouldNotLoginWhenCredentialsAreInvalid() throws Exception {
        LoginRequestDto request = new LoginRequestDto(
                "pedro@gmail.com",
                "senha-incorreta"
        );

        when(authService.localLogin(request)).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Incorrect email or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).localLogin(request);
    }

    @Test
    @DisplayName("Should return 403 Forbidden when the account is disabled")
    void shouldNotLoginWhenAccountIsDisabled() throws Exception {
        LoginRequestDto request = new LoginRequestDto(
                "pedro@gmail.com",
                "Senha123!"
        );

        doThrow(new org.springframework.security.authentication.DisabledException(
                "Please verify your email address before signing in"
        )).when(authService).localLogin(request);

        mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.error").value("ACCOUNT_DISABLED"))
                .andExpect(jsonPath("$.message").value("Please verify your email address before signing in"))
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).localLogin(request);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login payload is invalid")
    void shouldNotLoginWithInvalidPayload() throws Exception {
        LoginRequestDto request = new LoginRequestDto(
                "email-invalido",
                ""
        );

        mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").value("email must be a valid email address"))
                .andExpect(jsonPath("$.errors.password").value("password is required"))
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when login body is missing")
    void shouldNotLoginWithoutRequestBody() throws Exception {
        mockMvc.perform(
                post("/auth/login")
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.path").value("/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 201 Created when registration data is valid")
    void shouldRegisterSuccessfully() throws Exception {
        UserCreationDto request = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "Senha123!"
        );

        Instant now = Instant.now();
        UserResponseDto response = new UserResponseDto(
                1L,
                "Pedro Paulo",
                "pedro@gmail.com",
                UserRole.STUDENT,
                false,
                now
        );

        when(authService.localRegister(any())).thenReturn(response);

        mockMvc.perform(
                post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Pedro Paulo"))
                .andExpect(jsonPath("$.email").value("pedro@gmail.com"))
                .andExpect(jsonPath("$.createdAt").value(now.toString()));
    }

    @Test
    @DisplayName("Should return 409 Conflict when email is already in use")
    void shouldNotRegisterWhenEmailAlreadyExists() throws Exception {
        UserCreationDto request = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "Senha123!"
        );

        when(authService.localRegister(any())).thenThrow(new EmailAlreadyInUseException());

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_IN_USE"))
                .andExpect(jsonPath("$.message").value("Email already in use"))
                .andExpect(jsonPath("$.path").value("/auth/register"))
                .andExpect(jsonPath("$.timestamp").exists());


        verify(authService).localRegister(any());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is missing")
    void shouldNotRegisterWithoutRequestBody() throws Exception {

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST_BODY"))
                .andExpect(jsonPath("$.path").value("/auth/register"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 415 Unsupported Media Type when Content-Type is not application/json")
    void shouldNotRegisterWithUnsupportedMediaType() throws Exception {

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_ATOM_XML)
                                .content("<user><name>Pedro</name></user>")
                )
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.statusCode").value(415))
                .andExpect(jsonPath("$.error").value("MEDIA_TYPE_NOT_SUPPORTED"))
                .andExpect(jsonPath("$.message").value("Content-Type is not supported. Use application/json"))
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is invalid")
    void shouldNotRegisterWithInvalidEmail() throws Exception {

        UserCreationDto request = new UserCreationDto(
                "Pedro Paulo",
                "email-invalido",
                "Senha123!"
        );

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").value("email must be a valid email address"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request and validation errors when required fields are empty")
    void shouldNotRegisterWithEmptyFields() throws Exception {

        UserCreationDto request = new UserCreationDto(
                "",
                "",
                ""
        );

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password is shorter than 8 characters")
    void shouldNotRegisterWithShortPassword() throws Exception {

        UserCreationDto request = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "123"
        );

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when fullName is shorter than 3 characters")
    void shouldNotRegisterWithShortFullName() throws Exception {

        UserCreationDto request = new UserCreationDto(
                "Jo",
                "pedro@gmail.com",
                "Senha123!"
        );

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").value("name must contain between 3 and 120 characters"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when password fails complexity requirements")
    void shouldNotRegisterWithWeakPassword() throws Exception {

        UserCreationDto request = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "password"
        );

        mockMvc.perform(
                        post("/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.password").value("password must contain at least one uppercase letter, one lowercase letter and one number"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 200 OK when email verification token is valid")
    void shouldVerifyEmailSuccessfully() throws Exception {
        String token = "token";

        doNothing().when(authService).verifyEmail(token);

        mockMvc.perform(
                post("/auth/verify-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Email succesfully verified"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when token parameter is missing")
    void shouldNotVerifyEmailWhenTokenMissing() throws Exception {

        mockMvc.perform(
                post("/auth/verify-email")
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Required parameter 'token' is missing"))
                .andExpect(jsonPath("$.path").value("/auth/verify-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when token is invalid")
    void shouldNotVerifyEmailWithInvalidToken() throws Exception {
        String token = "invalid";

        doThrow(new InvalidTokenException("The token is not valid.")).when(authService).verifyEmail(token);

        mockMvc.perform(
                post("/auth/verify-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("The token is not valid."))
                .andExpect(jsonPath("$.path").value("/auth/verify-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token is expired")
    void shouldNotVerifyEmailWithExpiredToken() throws Exception {
        String token = "expired";

        doThrow(new ExpiredTokenException("The token is expired")).when(authService).verifyEmail(token);

        mockMvc.perform(
                post("/auth/verify-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"))
                .andExpect(jsonPath("$.message").value("The token is expired"))
                .andExpect(jsonPath("$.path").value("/auth/verify-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is already verified")
    void shouldNotVerifyEmailWhenAlreadyVerified() throws Exception {
        String token = "used";

        doThrow(new EmailAlreadyVerifiedException("The email is already verified.")).when(authService).verifyEmail(token);

        mockMvc.perform(
                post("/auth/verify-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_VERIFIED"))
                .andExpect(jsonPath("$.message").value("The email is already verified."))
                .andExpect(jsonPath("$.path").value("/auth/verify-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Should return 200 OK when resend verification email is requested")
    void shouldResendVerificationEmailSuccessfully() throws Exception {
        String email = "user@example.com";

        doNothing().when(authService).resendVerificationEmail(email);

        mockMvc.perform(
                post("/auth/resend-verification-email")
                        .with(csrf())
                        .param("email", email)
        )
                .andExpect(status().isOk())
                .andExpect(content().string("Verification email resent"));

        verify(authService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when resend email token is not found")
    void shouldNotResendVerificationEmailWhenNoToken() throws Exception {
        String email = "notfound@example.com";

        doThrow(new InvalidTokenException("The token is not valid.")).when(authService).resendVerificationEmail(email);

        mockMvc.perform(
                post("/auth/resend-verification-email")
                        .with(csrf())
                        .param("email", email)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("The token is not valid."))
                .andExpect(jsonPath("$.path").value("/auth/resend-verification-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when resend requested for already verified email")
    void shouldNotResendVerificationEmailWhenAlreadyVerified() throws Exception {
        String email = "used@example.com";

        doThrow(new EmailAlreadyVerifiedException("The email is already verified.")).when(authService).resendVerificationEmail(email);

        mockMvc.perform(
                post("/auth/resend-verification-email")
                        .with(csrf())
                        .param("email", email)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_VERIFIED"))
                .andExpect(jsonPath("$.message").value("The email is already verified."))
                .andExpect(jsonPath("$.path").value("/auth/resend-verification-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).resendVerificationEmail(email);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email parameter is missing for resend")
    void shouldNotResendVerificationEmailWhenEmailMissing() throws Exception {

        mockMvc.perform(
                post("/auth/resend-verification-email")
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/auth/resend-verification-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 200 OK when exchange code is valid")
    void shouldExchangeCodeSuccessfully() throws Exception {
        String code = "valid-code";

        LoginResponseDto response = new LoginResponseDto(
                "access-token",
                "refresh-token"
        );

        when(authService.exchange(code)).thenReturn(response);

        mockMvc.perform(
                        post("/auth/oauth2/exchange")
                                .with(csrf())
                                .param("code", code)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).exchange(code);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when exchange code is invalid")
    void shouldNotExchangeWhenCodeIsInvalid() throws Exception {

        String code = "invalid-code";

        when(authService.exchange(code)).thenThrow(new InvalidTokenException("Invalid exchange code"));

        mockMvc.perform(
                        post("/auth/oauth2/exchange")
                                .with(csrf())
                                .param("code", code)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("Invalid exchange code"))
                .andExpect(jsonPath("$.path").value("/auth/oauth2/exchange"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).exchange(code);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when exchange code is expired")
    void shouldNotExchangeWhenCodeIsExpired() throws Exception {

        String code = "expired-code";

        when(authService.exchange(code)).thenThrow(new ExpiredTokenException("Expired exchange token"));

        mockMvc.perform(
                        post("/auth/oauth2/exchange")
                                .with(csrf())
                                .param("code", code)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"))
                .andExpect(jsonPath("$.message").value("Expired exchange token"))
                .andExpect(jsonPath("$.path").value("/auth/oauth2/exchange"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).exchange(code);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when code parameter is missing")
    void shouldNotExchangeWhenCodeParameterIsMissing() throws Exception {

        mockMvc.perform(
                        post("/auth/oauth2/exchange")
                                .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("MISSING_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required parameter 'code' is missing"))
                .andExpect(jsonPath("$.path").value("/auth/oauth2/exchange"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 405 Method Not Allowed when GET is used instead of POST")
    void shouldReturnMethodNotAllowedForGetExchange() throws Exception {

        mockMvc.perform(
                        get("/auth/oauth2/exchange")
                )
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.statusCode").value(405))
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Method 'GET' is not allowed for this endpoint"))
                .andExpect(jsonPath("$.path").value("/auth/oauth2/exchange"));

        verifyNoInteractions(authService);
    }
}