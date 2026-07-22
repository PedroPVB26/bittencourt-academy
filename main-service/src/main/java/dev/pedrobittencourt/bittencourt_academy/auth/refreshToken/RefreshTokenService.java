package dev.pedrobittencourt.bittencourt_academy.auth.refreshToken;

import dev.pedrobittencourt.bittencourt_academy.user.User;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
        refreshToken.setRefreshToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }
}
