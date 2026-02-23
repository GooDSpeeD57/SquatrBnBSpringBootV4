package training.afpa.cda24060.squartrbnb.service;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;

@Service
@NullMarked
@Log4j2
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    /**
     * Récupère un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer id) {  // Non-null par défaut grâce à @NullMarked
        log.info("Recherche de l'utilisateur avec l'ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        return userMapper.toResponseDTO(user);
    }

    /**
     * Récupère tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");

        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouvel utilisateur
     */
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {  // Non-null par défaut
        log.info("Création d'un nouvel utilisateur: {}", userCreateDTO.getUsername());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new DataConflictException("Un utilisateur avec cet email existe déjà: " + userCreateDTO.getEmail());
        }

        // Vérifier si le username existe déjà
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new DataConflictException("Un utilisateur avec ce nom d'utilisateur existe déjà: " + userCreateDTO.getUsername());
        }

        // Convertir DTO en entité
        User user = userMapper.toEntity(userCreateDTO);

        // Encoder le mot de passe
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        // Attribuer le rôle
        Role role;
        if (userCreateDTO.getRoleId() != null) {
            role = roleRepository.findById(userCreateDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", userCreateDTO.getRoleId()));
        } else {
            // Rôle par défaut
            role = roleRepository.findByName("UTILISATEUR")
                    .orElseThrow(() -> new IllegalStateException("Rôle UTILISATEUR non trouvé en base de données"));
        }
        user.setRole(role);

        try {
            User savedUser = userRepository.save(user);
            log.info("Utilisateur créé avec succès: {}", savedUser.getId());
            return userMapper.toResponseDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Erreur d'intégrité lors de la création de l'utilisateur", e);
            throw new DataConflictException("Erreur lors de la création de l'utilisateur: données en conflit");
        }
    }

    /**
     * Met à jour un utilisateur existant
     */
    public UserResponseDTO updateUser(Integer id, UserUpdateDTO userUpdateDTO) {  // Non-null par défaut
        log.info("Mise à jour de l'utilisateur avec l'ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        // Vérifier si le nouvel email existe déjà (pour un autre utilisateur)
        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userUpdateDTO.getEmail())) {
                throw new DataConflictException("Un utilisateur avec cet email existe déjà: " + userUpdateDTO.getEmail());
            }
        }

        // Vérifier si le nouveau username existe déjà (pour un autre utilisateur)
        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userUpdateDTO.getUsername())) {
                throw new DataConflictException("Un utilisateur avec ce nom d'utilisateur existe déjà: " + userUpdateDTO.getUsername());
            }
        }

        // Mettre à jour les champs
        userMapper.updateEntityFromDTO(userUpdateDTO, user);

        // Mettre à jour le mot de passe si fourni
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
            log.info("Mot de passe mis à jour pour l'utilisateur: {}", id);
        }

        // Mettre à jour le rôle si fourni
        if (userUpdateDTO.getRoleId() != null) {
            Role role = roleRepository.findById(userUpdateDTO.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle", "id", userUpdateDTO.getRoleId()));
            user.setRole(role);
        }

        try {
            User updatedUser = userRepository.save(user);
            log.info("Utilisateur mis à jour avec succès: {}", updatedUser.getId());
            return userMapper.toResponseDTO(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Erreur d'intégrité lors de la mise à jour de l'utilisateur", e);
            throw new DataConflictException("Erreur lors de la mise à jour de l'utilisateur: données en conflit");
        }
    }

    /**
     * Supprime un utilisateur
     */
    public void deleteUser(Integer id) {  // Non-null par défaut
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", "id", id);
        }

        userRepository.deleteById(id);
        log.info("Utilisateur supprimé avec succès: {}", id);
    }

    /**
     * Recherche un utilisateur par email
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {  // Non-null par défaut
        log.info("Recherche de l'utilisateur avec l'email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        return userMapper.toResponseDTO(user);
    }

    /**
     * Recherche un utilisateur par username
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {  // Non-null par défaut
        log.info("Recherche de l'utilisateur avec le username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "username", username));

        return userMapper.toResponseDTO(user);
    }
}
