package dev.pedrobittencourt.bittencourt_academy.user;

import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STUDENT;

    @Column(nullable = false)
    private boolean enabled = false; // Determines if the user account is active (e.g., email verified, not suspended, not disabled).

    private Instant lastPasswordChangeAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public User(UserCreationDto dto) {
        this.fullName = dto.fullName();
        this.email = dto.email();
        this.password = dto.password();
    }
}
