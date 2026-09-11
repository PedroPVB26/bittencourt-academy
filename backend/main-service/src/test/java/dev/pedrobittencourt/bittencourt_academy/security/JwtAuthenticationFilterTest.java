package dev.pedrobittencourt.bittencourt_academy.security;

import dev.pedrobittencourt.bittencourt_academy.user.CustomUserDetailsService;
import dev.pedrobittencourt.bittencourt_academy.user.CustomUserPrincipal;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should continue filter chain when authorization header is missing")
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {

        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(customUserDetailsService);
    }

    @Test
    @DisplayName("Should continue filter chain when authorization header is not bearer")
    void shouldContinueFilterChainWhenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(customUserDetailsService);
    }

    @Test
    @DisplayName("Should authenticate user when token is valid")
    void shouldAuthenticateUserWhenTokenIsValid()
            throws ServletException, IOException {

        User user = new User();
        user.setEmail("pedro@email.com");

        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        when(jwtService.extractEmail("token")).thenReturn("pedro@email.com");

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        when(customUserDetailsService.loadUserByUsername("pedro@email.com")).thenReturn(principal);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(
                principal,
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should delegate exceptions to handler exception resolver")
    void shouldDelegateExceptionsToHandlerExceptionResolver()
            throws ServletException, IOException {

        RuntimeException exception = new RuntimeException("erro");

        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        when(jwtService.extractEmail("token")).thenThrow(exception);

        jwtAuthenticationFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(handlerExceptionResolver)
                .resolveException(
                        eq(request),
                        eq(response),
                        isNull(),
                        eq(exception)
                );

        verify(filterChain, never())
                .doFilter(any(), any());
    }
}