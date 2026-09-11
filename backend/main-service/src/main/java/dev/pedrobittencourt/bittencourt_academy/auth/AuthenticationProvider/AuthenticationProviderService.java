package dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider;

import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.AuthenticationProviderNotFoundException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidCredentialsException;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationProviderService {
    private final AuthenticationProviderRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticationProvider createLocalProvider(User user, String rawPassword) {
        AuthenticationProvider provider = new AuthenticationProvider();
        provider.setUser(user);
        provider.setProvider(AuthenticationProviderType.LOCAL);
        provider.setPasswordHash(passwordEncoder.encode(rawPassword));
        return repository.save(provider);
    }

    @Transactional
    public AuthenticationProvider createGoogleProvider(User user, String sub){
        AuthenticationProvider provider = new AuthenticationProvider();
        provider.setUser(user);
        provider.setProvider(AuthenticationProviderType.GOOGLE);
        provider.setProviderUserId(sub);
        return repository.save(provider);
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticationProvider> findLocalProvider(User user) {
        return repository.findByUserAndProvider(user, AuthenticationProviderType.LOCAL);
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticationProvider> findGoogleProvider(
            String googleSubject
    ) {
        return repository.findWithUser(AuthenticationProviderType.GOOGLE, googleSubject);
    }
}
