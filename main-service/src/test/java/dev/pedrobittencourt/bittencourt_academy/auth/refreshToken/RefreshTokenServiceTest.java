package dev.pedrobittencourt.bittencourt_academy.auth.refreshToken;

import dev.pedrobittencourt.bittencourt_academy.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenExpiration",
                604800000L // 7 dias
        );
    }

    @Test
    void shouldGenerateAndSaveRefreshToken() {
        User user = new User();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();

        RefreshToken result = refreshTokenService.generateRefreshToken(user);

        Instant after = Instant.now();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getRefreshToken());
        assertFalse(savedToken.getRefreshToken().isBlank());

        assertTrue(savedToken.getExpiryDate().isAfter(before));
        assertTrue(savedToken.getExpiryDate().isBefore(after.plusMillis(604800000L)));

        assertSame(savedToken, result);

        System.out.println("before = " + before);
        System.out.println("after = " + after);
        System.out.println("expiry = " + savedToken.getExpiryDate());
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentCalls() {
        User user = new User();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken first = refreshTokenService.generateRefreshToken(user);

        RefreshToken second = refreshTokenService.generateRefreshToken(user);

        assertNotEquals(
                first.getRefreshToken(),
                second.getRefreshToken()
        );
    }
}