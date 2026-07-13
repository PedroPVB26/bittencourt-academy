package dev.pedrobittencourt.bittencourt_academy.User;

import dev.pedrobittencourt.bittencourt_academy.User.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.User.dto.UserResponseDto;
import dev.pedrobittencourt.bittencourt_academy.exception.EmailAlreadyInUseException;
import dev.pedrobittencourt.bittencourt_academy.exception.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    protected User findEntityByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException("No user found with email " + email)
                );
    }

    @Transactional(readOnly = true)
    protected User findEntityById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("No user found with id " + userId)
                );
    }

    @Transactional
    public UserResponseDto save(UserCreationDto userCreationDto){
        if(userRepository.existsByEmail(userCreationDto.email())){
            throw new EmailAlreadyInUseException("Email already in use");
        }

        User user = new User();

        user.setFullName(userCreationDto.fullName());
        user.setEmail(userCreationDto.email());
        user.setEnabled(false);

        String encodedPassword = passwordEncoder.encode(userCreationDto.password());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }

    @Transactional
    public void delete(Long userId){
        userRepository.deleteById(userId);
    }

}
