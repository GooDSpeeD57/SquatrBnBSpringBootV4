package training.afpa.cda24060.squartrbnb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import training.afpa.cda24060.squartrbnb.dto.UserCreateDTO;
import training.afpa.cda24060.squartrbnb.dto.UserMapper;
import training.afpa.cda24060.squartrbnb.dto.UserResponseDTO;
import training.afpa.cda24060.squartrbnb.entity.Role;
import training.afpa.cda24060.squartrbnb.entity.User;
import training.afpa.cda24060.squartrbnb.exception.DataConflictException;
import training.afpa.cda24060.squartrbnb.exception.ResourceNotFoundException;
import training.afpa.cda24060.squartrbnb.repository.RoleRepository;
import training.afpa.cda24060.squartrbnb.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserCreateDTO userCreateDTO;
    private User user;
    private Role role;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        // Configuration du rôle
        role = new Role();
        role.setId(1);
        role.setName("UTILISATEUR");

        // Configuration du DTO de création
        userCreateDTO = UserCreateDTO.builder()
                .username("johndoe")
                .nom("Doe")
                .prenom("John")
                .email("john.doe@example.com")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .password("Password123!")
                .build();

        // Configuration de l'entité User
        user = new User();
        user.setId(1);
        user.setUsername("johndoe");
        user.setNom("Doe");
        user.setPrenom("John");
        user.setEmail("john.doe@example.com");
        user.setDateNaissance(LocalDate.of(1990, 1, 1));
        user.setPassword("encodedPassword");
        user.setRole(role);

        // Configuration du DTO de réponse
        userResponseDTO = UserResponseDTO.builder()
                .id(1)
                .username("johndoe")
                .nom("Doe")
                .prenom("John")
                .email("john.doe@example.com")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_WithValidData_ShouldReturnUserResponseDTO() {
        // Arrange
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userCreateDTO.getUsername())).thenReturn(false);
        when(userMapper.toEntity(userCreateDTO)).thenReturn(user);
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("UTILISATEUR")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = userService.createUser(userCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(userCreateDTO.getPassword());
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowDataConflictException() {
        // Arrange
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DataConflictException.class, () -> {
            userService.createUser(userCreateDTO);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowDataConflictException() {
        // Arrange
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userCreateDTO.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(DataConflictException.class, () -> {
            userService.createUser(userCreateDTO);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUserResponseDTO() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = userService.getUserById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("johndoe", result.getUsername());
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999);
        });
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        // Arrange
        when(userRepository.existsById(1)).thenReturn(true);

        // Act
        userService.deleteUser(1);

        // Assert
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(999);
        });

        verify(userRepository, never()).deleteById(anyInt());
    }
}
