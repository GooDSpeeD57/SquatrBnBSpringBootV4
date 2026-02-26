package training.afpa.cda24060.squartrbnb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.afpa.cda24060.squartrbnb.dto.UserCreateDTO;
import training.afpa.cda24060.squartrbnb.dto.UserMapper;
import training.afpa.cda24060.squartrbnb.dto.UserResponseDTO;
import training.afpa.cda24060.squartrbnb.dto.UserUpdateDTO;
import training.afpa.cda24060.squartrbnb.entity.Role;
import training.afpa.cda24060.squartrbnb.entity.User;
import training.afpa.cda24060.squartrbnb.exception.DataConflictException;
import training.afpa.cda24060.squartrbnb.exception.ResourceNotFoundException;
import training.afpa.cda24060.squartrbnb.repository.RoleRepository;
import training.afpa.cda24060.squartrbnb.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer id) {
        log.info("Recherche utilisateur id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    public UserResponseDTO createUser(UserCreateDTO dto) {
        log.info("Création utilisateur: {}", dto.getUsername());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DataConflictException("Email déjà utilisé: " + dto.getEmail());
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DataConflictException("Username déjà utilisé: " + dto.getUsername());
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(resolveRole(dto.getRoleId()));

        try {
            User saved = userRepository.save(user);
            log.info("Utilisateur créé id={}", saved.getId());
            return userMapper.toResponseDTO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("Conflit lors de la création de l'utilisateur");
        }
    }

    public UserResponseDTO updateUser(Integer id, UserUpdateDTO dto) {
        log.info("Mise à jour utilisateur id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new DataConflictException("Email déjà utilisé: " + dto.getEmail());
        }
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new DataConflictException("Username déjà utilisé: " + dto.getUsername());
        }

        userMapper.updateEntityFromDTO(dto, user);

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            log.info("Mot de passe mis à jour pour id={}", id);
        }
        if (dto.getRoleId() != null) {
            user.setRole(resolveRole(dto.getRoleId()));
        }

        try {
            User updated = userRepository.save(user);
            log.info("Utilisateur mis à jour id={}", updated.getId());
            return userMapper.toResponseDTO(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("Conflit lors de la mise à jour de l'utilisateur");
        }
    }

    public void deleteUser(Integer id) {
        log.info("Suppression utilisateur id={}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", "id", id);
        }
        userRepository.deleteById(id);
        log.info("Utilisateur supprimé id={}", id);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.info("Recherche utilisateur email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
        return userMapper.toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        log.info("Recherche utilisateur username={}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));
        return userMapper.toResponseDTO(user);
    }

    private Role resolveRole(Integer roleId) {
        if (roleId != null) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", roleId));
        }
        return roleRepository.findByName("UTILISATEUR")
                .orElseThrow(() -> new IllegalStateException("Rôle UTILISATEUR introuvable"));
    }
}