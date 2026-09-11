package dev.pedrobittencourt.bittencourt_academy.security;

import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidTokenException;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import dev.pedrobittencourt.bittencourt_academy.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Instant fixedNow;
    private static final Long ACCESS_TOKEN_EXPIRATION = 900000L;
    private static final String SECRET = Base64.getEncoder()
                    .encodeToString("12345678901234567890123456789012".getBytes());

    @BeforeEach
    void setUp() {
        fixedNow = Instant.now();

        Clock fixedClock = Clock.fixed(
                fixedNow,
                ZoneOffset.UTC
        );

        jwtService = new JwtService(fixedClock);

        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                SECRET
        );

        ReflectionTestUtils.setField(
                jwtService,
                "accessTokenExpiration",
                ACCESS_TOKEN_EXPIRATION
        );
    }

    @Test
    @DisplayName("Should generate JWT with correct subject claim")
    void shouldGenerateTokenWithCorrectSubjectClaim() {
        User user = createUser();

        String token = jwtService.generateAccessToken(user);

        Claims claims = extractClaims(token);

        assertEquals(
                user.getEmail(),
                claims.getSubject()
        );
    }

    @Test
    @DisplayName("Should generate JWT with correct custom claims")
    void shouldGenerateTokenWithCorrectCustomClaims() {
        User user = createUser();

        String token = jwtService.generateAccessToken(user);

        Claims claims = extractClaims(token);

        assertEquals(
                user.getId().intValue(),
                claims.get("id", Integer.class)
        );

        assertEquals(
                user.getRole().name(),
                claims.get("role", String.class)
        );
    }

    @Test
    @DisplayName("Should generate JWT with configured expiration")
    void shouldGenerateTokenWithConfiguredExpiration() {
        User user = createUser();

        String token = jwtService.generateAccessToken(user);

        Claims claims = extractClaims(token);

        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiration = claims.getExpiration().toInstant();

        assertEquals(
                fixedNow.getEpochSecond(),
                issuedAt.getEpochSecond()
        );

        assertEquals(
                fixedNow.plusMillis(ACCESS_TOKEN_EXPIRATION).getEpochSecond(),
                expiration.getEpochSecond()
        );
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void shouldExtractEmailFromValidToken() {
        User user = createUser();

        String token = jwtService.generateAccessToken(user);
        String email = jwtService.extractEmail(token);

        assertEquals(
                user.getEmail(),
                email
        );
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is malformed")
    void shouldThrowInvalidTokenExceptionWhenTokenIsInvalid() {
        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.extractEmail("token-invalido")
        );
    }

    @Test
    @DisplayName("Should throw ExpiredTokenException when token is expired")
    void shouldThrowExpiredTokenExceptionWhenTokenIsExpired() {
        User user = createUser();

        ReflectionTestUtils.setField(
                jwtService,
                "accessTokenExpiration",
                -1000L
        );

        String token = jwtService.generateAccessToken(user);

        assertThrows(
                ExpiredTokenException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("pedro@email.com");
        user.setRole(UserRole.STUDENT);
        return user;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}