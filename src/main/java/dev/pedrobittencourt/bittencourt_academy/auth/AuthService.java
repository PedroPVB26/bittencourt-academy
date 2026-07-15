package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationToken;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationTokenRepository;
import dev.pedrobittencourt.bittencourt_academy.email.EmailService;
import dev.pedrobittencourt.bittencourt_academy.exception.EmailAlreadyVerifiedException;
import dev.pedrobittencourt.bittencourt_academy.exception.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.InvalidTokenException;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import dev.pedrobittencourt.bittencourt_academy.user.UserService;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserService userService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto register(UserCreationDto  userCreationDto) {
        User savedUser = userService.save(userCreationDto);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken tokenEntity = new EmailVerificationToken();
        tokenEntity.setToken(token);
        tokenEntity.setUser(savedUser);
        tokenEntity.setExpiresAt(Instant.now().plus(48, ChronoUnit.HOURS));

        emailVerificationTokenRepository.save(tokenEntity);

        String link = "http://localhost:8080/auth/verify-email?token=" + token;

        emailService.sendVerificationEmail(
                savedUser.getEmail(), link, savedUser.getFullName()
        );

        return new UserResponseDto(savedUser);
    }

    @Transactional
    public void verifiyEmail(String token) {

        // Verificar se o token é válido ou não
        EmailVerificationToken tokenEntity = emailVerificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("The token is not valid."));

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new ExpiredTokenException("The token is expired");
        }

        // Verificar se o email já está verificado ou não
        if (tokenEntity.isUsed()){
            throw new EmailAlreadyVerifiedException("The email is already verified.");
        }

        tokenEntity.setUsed(true);
        tokenEntity.getUser().setEnabled(true);
        emailVerificationTokenRepository.save(tokenEntity);
    }
}
