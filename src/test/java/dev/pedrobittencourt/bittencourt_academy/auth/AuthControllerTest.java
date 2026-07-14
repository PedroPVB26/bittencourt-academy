package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.security.SecurityConfig;
import dev.pedrobittencourt.bittencourt_academy.user.UserRole;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void shouldRegister() throws Exception {
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

    // Ainda tem que implementar testes para quando o usuário não enviar nenhum corpo, enviar dados errados/inválidos
    // enviar email já existente e verificar todas as mensagens de erro
}