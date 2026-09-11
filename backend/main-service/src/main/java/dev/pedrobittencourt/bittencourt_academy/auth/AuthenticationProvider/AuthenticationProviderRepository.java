package dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider;

import dev.pedrobittencourt.bittencourt_academy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthenticationProviderRepository extends JpaRepository<AuthenticationProvider,Long> {
    Optional<AuthenticationProvider> findByUserAndProvider(User user, AuthenticationProviderType provider);

    Optional<AuthenticationProvider> findByProviderAndProviderUserId(
            AuthenticationProviderType provider, String providerUserId
    );

    @Query("""
    SELECT p
    FROM AuthenticationProvider p
    JOIN FETCH p.user
    WHERE p.provider = :provider
    AND p.providerUserId = :providerUserId
    """)
    Optional<AuthenticationProvider> findWithUser(
            AuthenticationProviderType provider,
            String providerUserId
    );
}
