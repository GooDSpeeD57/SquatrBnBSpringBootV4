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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserService userService;

    private UserCreateDTO dto;
    private User user;
    private Role role;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("UTILISATEUR");

        dto = UserCreateDTO.builder()
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

        responseDTO = UserResponseDTO.builder()
                .id(1)
                .username("johndoe")
                .nom("Doe")
                .prenom("John")
                .email("john.doe@example.com")
                .dateNaissance(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_validData_returnsDTO() {
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("UTILISATEUR")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(dto.getPassword());
    }

    @Test
    void createUser_existingEmail_throwsConflict() {
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThrows(DataConflictException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_existingUsername_throwsConflict() {
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);
        assertThrows(DataConflictException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_exists_returnsDTO() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(responseDTO);

        UserResponseDTO result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999));
    }

    @Test
    void deleteUser_exists_deletesSuccessfully() {
        when(userRepository.existsById(1)).thenReturn(true);
        userService.deleteUser(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userRepository.existsById(999)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999));
        verify(userRepository, never()).deleteById(anyInt());
    }
}
