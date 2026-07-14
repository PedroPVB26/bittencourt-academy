package dev.pedrobittencourt.bittencourt_academy.auth;

import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
