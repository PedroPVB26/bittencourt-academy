package dev.pedrobittencourt.bittencourt_academy.security;

import dev.pedrobittencourt.bittencourt_academy.user.User;
import dev.pedrobittencourt.bittencourt_academy.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should return user when email exists")
    void shouldReturnUserWhenEmailExists() {

        User user = new User();
        user.setEmail("pedro@email.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("pedro@email.com");

        assertNotNull(result);
        assertEquals("pedro@email.com", result.getUsername());

        verify(userRepository).findByEmail("pedro@email.com");
    }


    @Test
    @DisplayName("Should throw exception when user does not exist")
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService
                        .loadUserByUsername("unknown@email.com")
        );

        verify(userRepository).findByEmail("unknown@email.com");
    }
}