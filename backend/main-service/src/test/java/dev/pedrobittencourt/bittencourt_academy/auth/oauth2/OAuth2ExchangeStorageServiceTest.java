package dev.pedrobittencourt.bittencourt_academy.auth.oauth2;

import dev.pedrobittencourt.bittencourt_academy.auth.model.OAuth2TokenExchange;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2ExchangeStorageServiceTest {

    private OAuth2ExchangeStorageService service;

    @BeforeEach
    void setUp() {
        service = new OAuth2ExchangeStorageService();
    }

    @Test
    @DisplayName("Should consume valid exchange code")
    void shouldConsumeValidCode() {

        OAuth2TokenExchange exchange = new OAuth2TokenExchange(
                1L, Instant.now().plusSeconds(30)
        );

        service.save("code", exchange);

        OAuth2TokenExchange result = service.consume("code");

        assertEquals(exchange, result);
    }

    @Test
    @DisplayName("Should not allow code reuse")
    void shouldNotAllowCodeReuse() {

        OAuth2TokenExchange exchange = new OAuth2TokenExchange(
                1L, Instant.now().plusSeconds(60)
        );

        service.save("code", exchange);
        service.consume("code");

        assertThrows(
                InvalidTokenException.class,
                () -> service.consume("code")
        );
    }

    @Test
    @DisplayName("Should throw exception when code does not exist")
    void shouldThrowExceptionWhenCodeDoesNotExist() {

        assertThrows(
                InvalidTokenException.class,
                () -> service.consume("unknown-code")
        );
    }

    @Test
    @DisplayName("Should throw exception when exchange code is expired")
    void shouldThrowExceptionWhenCodeIsExpired() {

        OAuth2TokenExchange exchange = new OAuth2TokenExchange(
                1L,
                Instant.now().minusSeconds(60)
        );

        service.save("expired-code", exchange);

        assertThrows(
                ExpiredTokenException.class,
                () -> service.consume("expired-code")
        );
    }

    @Test
    @DisplayName("Should remove expired exchange code after expiration")
    void shouldRemoveExpiredCodeAfterExpiration() {

        OAuth2TokenExchange exchange = new OAuth2TokenExchange(
                1L,
                Instant.now().minusSeconds(60)
        );

        service.save("expired-code", exchange);

        assertThrows(
                ExpiredTokenException.class,
                () -> service.consume("expired-code")
        );

        assertThrows(
                InvalidTokenException.class,
                () -> service.consume("expired-code")
        );
    }
}