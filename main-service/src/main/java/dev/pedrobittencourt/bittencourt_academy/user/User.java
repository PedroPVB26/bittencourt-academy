package dev.pedrobittencourt.bittencourt_academy.user;

import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STUDENT;

    @Column(nullable = false)
    private boolean enabled = false; // Determines if the user account is active (e.g., email verified, not suspended, not disabled).
//
//    private Instant lastPasswordChangeAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    public User(UserCreationDto dto) {
        this.fullName = dto.fullName();
        this.email = dto.email();
    }
}
