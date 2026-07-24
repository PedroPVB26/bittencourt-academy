package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.AppProperties;
import dev.pedrobittencourt.bittencourt_academy.auth.dto.LoginRequestDto;
import dev.pedrobittencourt.bittencourt_academy.auth.dto.LoginResponseDto;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationToken;
import dev.pedrobittencourt.bittencourt_academy.auth.emailVerificationToken.EmailVerificationTokenRepository;
import dev.pedrobittencourt.bittencourt_academy.auth.refreshToken.RefreshTokenService;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyVerifiedException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.ExpiredTokenException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidCredentialsException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.InvalidTokenException;
import dev.pedrobittencourt.bittencourt_academy.messaging.EmailPublisher;
import dev.pedrobittencourt.bittencourt_academy.security.JwtService;
import dev.pedrobittencourt.bittencourt_academy.user.User;
import dev.pedrobittencourt.bittencourt_academy.user.UserService;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final AppProperties appProperties;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailPublisher emailPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userService.findEntityByEmail(loginRequestDto.email())
                .orElseThrow(InvalidCredentialsException::new);


        boolean passwordMatches = passwordEncoder.matches(loginRequestDto.password(), user.getPassword());

        if(!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()){
            throw new DisabledException("Please verify your email address before signing in");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user).getRefreshToken();
        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Transactional
    public UserResponseDto register(UserCreationDto  userCreationDto) {
        User savedUser = userService.save(userCreationDto);

        EmailVerificationToken tokenEntity = generateToken(savedUser);
        emailVerificationTokenRepository.save(tokenEntity);

        String link = generateEmailVerificationLink(tokenEntity.getToken());
        emailPublisher.sendVerificationEmail(savedUser.getEmail(), link, savedUser.getFullName());

        return new UserResponseDto(savedUser);
    }

    @Transactional
    public void verifyEmail(String token) {

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

    @Transactional
    public void resendVerificationEmail(String email){
        EmailVerificationToken oldTtoken = emailVerificationTokenRepository
                .findByUserEmail(email)
                .orElseThrow(() -> new InvalidTokenException("The token is not valid."));

        if(oldTtoken.isUsed()){
            throw new EmailAlreadyVerifiedException("The email is already verified.");
        }

        emailVerificationTokenRepository.delete(oldTtoken);

        User user = oldTtoken.getUser();

        EmailVerificationToken newToken = generateToken(user);
        emailVerificationTokenRepository.save(newToken);

        String link = generateEmailVerificationLink(newToken.getToken());
        emailPublisher.sendVerificationEmail(user.getEmail(), link, user.getFullName());
    }

    private EmailVerificationToken generateToken(User user){
        String newToken = UUID.randomUUID().toString();
        EmailVerificationToken tokenEntity = new EmailVerificationToken();
        tokenEntity.setToken(newToken);
        tokenEntity.setUser(user);
        tokenEntity.setExpiresAt(Instant.now().plus(48, ChronoUnit.HOURS));
        return tokenEntity;
    }

    private String generateEmailVerificationLink(String token){
        return appProperties.backendUrl() + "/auth/verify-email?token=" + token;
    }
}
