package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.auth.model.dto.LoginRequestDto;
import dev.pedrobittencourt.bittencourt_academy.auth.model.dto.LoginResponseDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserCreationDto dto){
        UserResponseDto userResponseDto = authService.localRegister(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token){
        authService.verifyEmail(token);
        return ResponseEntity.ok(
                Map.of("message", "Email succesfully verified")
        );
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email){
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Verification email resent");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        return ResponseEntity.ok(authService.localLogin(loginRequestDto));
    }

    @PostMapping("/oauth2/exchange")
    public ResponseEntity<LoginResponseDto> exchange(@RequestParam String code){
        return ResponseEntity.ok(authService.exchange(code));
    }
}
