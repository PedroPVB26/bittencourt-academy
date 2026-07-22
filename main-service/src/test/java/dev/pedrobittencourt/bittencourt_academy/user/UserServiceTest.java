package dev.pedrobittencourt.bittencourt_academy.user;

import dev.pedrobittencourt.bittencourt_academy.user.dto.UserCreationDto;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.EmailAlreadyInUseException;
import dev.pedrobittencourt.bittencourt_academy.exception.exceptionsTypes.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should find an user by id")
    void findEntityByIdWithExistingId() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setFullName("Pedro Paulo");
        user.setEmail("pedro@gmail.com");

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        User result = userService.findEntityById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should find an user by email")
    void findEntityByEmailWithExistingEmail() {
        User user = new User();
        user.setId(1L);
        user.setFullName("Pedro Paulo");
        user.setEmail("pedro@gmail.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        User result = userService.findEntityByEmail(user.getEmail());

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getFullName()).isEqualTo(user.getFullName());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    @DisplayName("Should not find an user by email - UserNotFoundException")
    void findEntityByEmailWithNonExistingEmail() {
        String email = "unknown@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> userService.findEntityByEmail(email)
        );

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should not find an user by id - UserNotFoundException")
    void findEntityByIdWithNonExistingId() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        assertThrows(
                UserNotFoundException.class, () -> userService.findEntityById(userId)
        );

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should successfully save an user")
    void saveWithNonUsedEmail() {
        UserCreationDto userCreationDto = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "MinhaSenha!@123"
        );

        when(userRepository.existsByEmail(userCreationDto.email())).thenReturn(false);
        when(passwordEncoder.encode(userCreationDto.password())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName(userCreationDto.fullName());
        savedUser.setEmail(userCreationDto.email());
        savedUser.setPassword("encodedPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.save(userCreationDto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userCreationDto.email());
        assertThat(result.getFullName()).isEqualTo(userCreationDto.fullName());

        verify(userRepository).existsByEmail(userCreationDto.email());
        verify(passwordEncoder).encode(userCreationDto.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should not save an user - EmailAlreadyInUse")
    void saveWithUsedEmail() {
        UserCreationDto userCreationDto = new UserCreationDto(
                "Pedro Paulo",
                "pedro@gmail.com",
                "MinhaSenha!@123"
        );

        when(userRepository.existsByEmail(userCreationDto.email())).thenReturn(true);


        assertThrows(
                EmailAlreadyInUseException.class, () -> userService.save(userCreationDto)
        );

        verify(userRepository).existsByEmail(userCreationDto.email());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("Should delete an user")
    void deleteExistingUser() {
        Long userId = 1L;

        doNothing()
                .when(userRepository)
                .deleteById(userId);

        userService.delete(userId);

        verify(userRepository)
                .deleteById(userId);
    }
}