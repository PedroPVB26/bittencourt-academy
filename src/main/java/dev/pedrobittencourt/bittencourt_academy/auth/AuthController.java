package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserCreationDto dto){
        UserResponseDto userResponseDto = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @PostMapping("/verifiy-email")
    public ResponseEntity<String> verifiyEmail(@RequestParam String token){
        authService.verifiyEmail(token);
        return ResponseEntity.ok("Email succesfully verified");
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email){
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok("Verification email resent");
    }
}
