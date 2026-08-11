package dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider;

import dev.pedrobittencourt.bittencourt_academy.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationProviderServiceTest {

    @InjectMocks
    private AuthenticationProviderService providerService;

    @Mock
    private AuthenticationProviderRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should create local authentication provider")
    void shouldCreateLocalProvider() {

        User user = new User();
        user.setId(1L);

        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        AuthenticationProvider savedProvider = new AuthenticationProvider();
        savedProvider.setUser(user);
        savedProvider.setProvider(AuthenticationProviderType.LOCAL);
        savedProvider.setPasswordHash("encoded-password");

        when(repository.save(any(AuthenticationProvider.class))).thenReturn(savedProvider);

        AuthenticationProvider result = providerService.createLocalProvider(user, "password123");

        assertNotNull(result);
        assertEquals(AuthenticationProviderType.LOCAL, result.getProvider());
        assertEquals("encoded-password", result.getPasswordHash());
        assertEquals(user, result.getUser());

        verify(passwordEncoder).encode("password123");
        verify(repository).save(any(AuthenticationProvider.class));
    }

    @Test
    @DisplayName("Should encode password before saving local provider")
    void shouldEncodePasswordBeforeSavingLocalProvider() {

        User user = new User();

        when(passwordEncoder.encode("raw-password")).thenReturn("hashed-password");

        providerService.createLocalProvider(user, "raw-password");

        ArgumentCaptor<AuthenticationProvider> captor = ArgumentCaptor.forClass(AuthenticationProvider.class);

        verify(repository).save(captor.capture());

        AuthenticationProvider saved = captor.getValue();

        assertEquals("hashed-password", saved.getPasswordHash());
        assertEquals(AuthenticationProviderType.LOCAL, saved.getProvider());
    }

    @Test
    @DisplayName("Should create Google authentication provider")
    void shouldCreateGoogleProvider() {

        User user = new User();
        user.setId(1L);

        AuthenticationProvider savedProvider = new AuthenticationProvider();
        savedProvider.setUser(user);
        savedProvider.setProvider(AuthenticationProviderType.GOOGLE);
        savedProvider.setProviderUserId("google-sub");

        when(repository.save(any(AuthenticationProvider.class))).thenReturn(savedProvider);

        AuthenticationProvider result = providerService.createGoogleProvider(user, "google-sub");

        assertNotNull(result);
        assertEquals(AuthenticationProviderType.GOOGLE, result.getProvider());
        assertEquals("google-sub", result.getProviderUserId());
        assertEquals(user, result.getUser());

        verify(repository).save(any(AuthenticationProvider.class));
    }

    @Test
    @DisplayName("Should save Google provider with correct data")
    void shouldSaveGoogleProviderWithCorrectData() {

        User user = new User();
        user.setId(1L);
        user.setFullName("Pedro Paulo");
        user.setEmail("pedro@gmail.com");
        user.setEnabled(true);

        AuthenticationProvider provider = new AuthenticationProvider();
        provider.setId(10L);
        provider.setUser(user);
        provider.setProvider(AuthenticationProviderType.GOOGLE);
        provider.setProviderUserId("google-sub");

        when(repository.save(any(AuthenticationProvider.class))).thenReturn(provider);

        AuthenticationProvider result = providerService.createGoogleProvider(user, "google-sub");

        ArgumentCaptor<AuthenticationProvider> captor = ArgumentCaptor.forClass(AuthenticationProvider.class);

        verify(repository).save(captor.capture());

        AuthenticationProvider saved = captor.getValue();

        assertEquals(AuthenticationProviderType.GOOGLE, saved.getProvider());
        assertEquals("google-sub", saved.getProviderUserId());

        assertEquals(1L, saved.getUser().getId());
        assertEquals("Pedro Paulo", saved.getUser().getFullName());
        assertEquals("pedro@gmail.com", saved.getUser().getEmail());

        assertEquals(provider, result);
    }

    @Test
    @DisplayName("Should find local authentication provider")
    void shouldFindLocalProvider() {
        User user = new User();

        AuthenticationProvider provider = new AuthenticationProvider();

        when(repository.findByUserAndProvider(
                user,
                AuthenticationProviderType.LOCAL
        )).thenReturn(Optional.of(provider));

        AuthenticationProvider result = providerService.findLocalProvider(user).get();

        assertEquals(provider, result);

        verify(repository).findByUserAndProvider(
                user,
                AuthenticationProviderType.LOCAL
        );
    }

    @Test
    @DisplayName("Should return empty when local authentication provider is not found")
    void shouldReturnEmptyWhenLocalProviderIsNotFound() {

        User user = new User();
        user.setId(1L);
        user.setEmail("pedro@gmail.com");

        when(repository.findByUserAndProvider(
                user,
                AuthenticationProviderType.LOCAL
        )).thenReturn(Optional.empty());

        Optional<AuthenticationProvider> result = providerService.findLocalProvider(user);

        assertTrue(result.isEmpty());

        verify(repository).findByUserAndProvider(
                user,
                AuthenticationProviderType.LOCAL
        );
    }

    @Test
    @DisplayName("Should find Google authentication provider")
    void shouldFindGoogleProvider() {

        AuthenticationProvider provider = new AuthenticationProvider();

        when(repository.findWithUser(
                AuthenticationProviderType.GOOGLE,
                "google-sub"
        )).thenReturn(Optional.of(provider));

        Optional<AuthenticationProvider> result =  providerService.findGoogleProvider("google-sub");

        assertTrue(result.isPresent());
        assertEquals(provider, result.get());

        verify(repository).findWithUser(
                AuthenticationProviderType.GOOGLE,
                "google-sub"
        );
    }

    @Test
    @DisplayName("Should return empty when Google authentication provider is not found")
    void shouldReturnEmptyWhenGoogleProviderIsNotFound() {

        when(repository.findWithUser(
                AuthenticationProviderType.GOOGLE,
                "google-sub"
        )).thenReturn(Optional.empty());

        Optional<AuthenticationProvider> result = providerService.findGoogleProvider("google-sub");

        assertTrue(result.isEmpty());

        verify(repository).findWithUser(
                AuthenticationProviderType.GOOGLE,
                "google-sub"
        );
    }
}