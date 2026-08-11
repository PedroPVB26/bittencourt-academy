package dev.pedrobittencourt.bittencourt_academy.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserPrincipalTest {
    @Test
    void shouldReturnUserEmailAsUsername() {
        User user = new User();
        user.setEmail("pedro@email.com");
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        assertEquals("pedro@email.com", principal.getUsername());
    }

    @Test
    void shouldReturnRoleAsGrantedAuthority() {
        User user = new User();
        user.setRole(UserRole.STUDENT);
        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(
                authorities.stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))
        );
    }

    @Test
    void shouldReturnNullPassword() {
        User user = new User();
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        assertNull(principal.getPassword());
    }
}