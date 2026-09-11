package dev.pedrobittencourt.bittencourt_academy.auth.AuthenticationProvider;

import dev.pedrobittencourt.bittencourt_academy.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authentication_providers")
@Getter
@Setter
@NoArgsConstructor
public class AuthenticationProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AuthenticationProviderType provider;

    private String providerUserId;

    private String passwordHash;
}
