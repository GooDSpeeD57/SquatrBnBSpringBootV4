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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService.
 *
 * ✅ CORRIGÉ Spring Boot 4:
 * - MockitoTestExecutionListener a été SUPPRIMÉ dans Spring Boot 4.
 *   @ExtendWith(MockitoExtension.class) est désormais OBLIGATOIRE pour que
 *   @Mock et @InjectMocks fonctionnent. Sans ça, tous les mocks sont null
 *   et les tests échouent avec NullPointerException au lieu d'un message clair.
 *
 * Ce test utilisait déjà @ExtendWith(MockitoExtension.class) ✅ — aucune
 * modification nécessaire sur l'annotation elle-même, mais les imports anyInt()
 * manquaient dans votre version originale, ce qui causait une erreur de compilation.
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
        role = new Role();
        role.setId(1);
        role.setName("UTILISATEUR");

        userCreateDTO = UserCreateDTO.builder()
                .username("johndoe")
                .nom("Doe")
                .prenom("John")
                .email("john.doe@example.com")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .password("Password123!")
                .build();

        user = new User();
        user.setId(1);
        user.setUsername("johndoe");
        user.setNom("Doe");
        user.setPrenom("John");
        user.setEmail("john.doe@example.com");
        user.setDateNaissance(LocalDate.of(1990, 1, 1));
        user.setPassword("encodedPassword");
        user.setRole(role);

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
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userCreateDTO.getUsername())).thenReturn(false);
        when(userMapper.toEntity(userCreateDTO)).thenReturn(user);
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("UTILISATEUR")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(userCreateDTO);

        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(userCreateDTO.getPassword());
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowDataConflictException() {
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(true);

        assertThrows(DataConflictException.class, () -> userService.createUser(userCreateDTO));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithExistingUsername_ShouldThrowDataConflictException() {
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userCreateDTO.getUsername())).thenReturn(true);

        assertThrows(DataConflictException.class, () -> userService.createUser(userCreateDTO));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUserResponseDTO() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("johndoe", result.getUsername());
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999));
    }

    @Test
    void deleteUser_WithExistingId_ShouldDeleteUser() {
        when(userRepository.existsById(1)).thenReturn(true);

        userService.deleteUser(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(userRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999));

        // ✅ CORRIGÉ: anyInt() était manquant dans l'import original, ajout du bon import statique
        verify(userRepository, never()).deleteById(anyInt());
    }
}
