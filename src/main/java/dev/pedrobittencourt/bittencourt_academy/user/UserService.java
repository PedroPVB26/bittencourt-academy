package dev.pedrobittencourt.bittencourt_academy.user;

import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyInUseException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findEntityByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException("No user found with email " + email)
                );
    }

    @Transactional(readOnly = true)
    public User findEntityById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("No user found with id " + userId)
                );
    }

    @Transactional
    public User save(UserCreationDto userCreationDto){
        if(userRepository.existsByEmail(userCreationDto.email())){
            throw new EmailAlreadyInUseException("Email already in use");
        }

        User user = new User();

        user.setFullName(userCreationDto.fullName());
        user.setEmail(userCreationDto.email());
        user.setEnabled(false);
        user.setCreatedAt(Instant.now());

        String encodedPassword = passwordEncoder.encode(userCreationDto.password());
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long userId){
        userRepository.deleteById(userId);
    }

}
