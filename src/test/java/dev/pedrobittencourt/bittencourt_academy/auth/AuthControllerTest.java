package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyInUseException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyVerifiedException;
import dev.pedrobittencourt.bittencourt_academy.security.SecurityConfig;
import dev.pedrobittencourt.bittencourt_academy.user.UserRole;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

        when(authService.register(any())).thenReturn(response);

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

        when(authService.register(any())).thenThrow(new EmailAlreadyInUseException());

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


        verify(authService).register(any());
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

    // ###########################################################################################################
    // Ainda tem que implementar testes para quando o usuário não enviar nenhum corpo, enviar dados errados/inválidos
    // enviar email já existente e verificar todas as mensagens de erro
    // ###########################################################################################################

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

        doNothing().when(authService).verifiyEmail(token);

        mockMvc.perform(
                post("/auth/verifiy-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isOk())
                .andExpect(
                        content().string("Email succesfully verified")
                );
    }

    @Test
    @DisplayName("Should return 400 Bad Request when token parameter is missing")
    void shouldNotVerifyEmailWhenTokenMissing() throws Exception {

        mockMvc.perform(
                post("/auth/verifiy-email")
                        .with(csrf())
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Required parameter 'token' is missing"))
                .andExpect(jsonPath("$.path").value("/auth/verifiy-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when token is invalid")
    void shouldNotVerifyEmailWithInvalidToken() throws Exception {
        String token = "invalid";

        doThrow(new InvalidTokenException("The token is not valid.")).when(authService).verifiyEmail(token);

        mockMvc.perform(
                post("/auth/verifiy-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("TOKEN_INVALID"))
                .andExpect(jsonPath("$.message").value("The token is not valid."))
                .andExpect(jsonPath("$.path").value("/auth/verifiy-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).verifiyEmail(token);
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token is expired")
    void shouldNotVerifyEmailWithExpiredToken() throws Exception {
        String token = "expired";

        doThrow(new ExpiredTokenException("The token is expired")).when(authService).verifiyEmail(token);

        mockMvc.perform(
                post("/auth/verifiy-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"))
                .andExpect(jsonPath("$.message").value("The token is expired"))
                .andExpect(jsonPath("$.path").value("/auth/verifiy-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).verifiyEmail(token);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when email is already verified")
    void shouldNotVerifyEmailWhenAlreadyVerified() throws Exception {
        String token = "used";

        doThrow(new EmailAlreadyVerifiedException("The email is already verified.")).when(authService).verifiyEmail(token);

        mockMvc.perform(
                post("/auth/verifiy-email")
                        .with(csrf())
                        .param("token", token)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_VERIFIED"))
                .andExpect(jsonPath("$.message").value("The email is already verified."))
                .andExpect(jsonPath("$.path").value("/auth/verifiy-email"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authService).verifiyEmail(token);
    }

    // ###########################################################################################################
    // FAZER OS TESTES NEGATIVOS
    // ###########################################################################################################
}