package training.afpa.cda24060.squartrbnb.controller;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import training.afpa.cda24060.squartrbnb.dto.UserCreateDTO;
import training.afpa.cda24060.squartrbnb.dto.UserResponseDTO;
import training.afpa.cda24060.squartrbnb.dto.UserUpdateDTO;
import training.afpa.cda24060.squartrbnb.service.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des utilisateurs
 */
@RestController
@RequestMapping(value = "/api/users", version = "1")
@Log4j2
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Crée un nouvel utilisateur
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        log.info("Requête de création d'utilisateur reçue: {}", userCreateDTO.getUsername());
        
        UserResponseDTO createdUser = userService.createUser(userCreateDTO);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    /**
     * Récupère tous les utilisateurs
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Requête de récupération de tous les utilisateurs");
        
        List<UserResponseDTO> users = userService.getAllUsers();
        
        return ResponseEntity.ok(users);
    }

    /**
     * Récupère un utilisateur par son ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable @NonNull Integer id) {
        log.info("Requête de récupération de l'utilisateur avec l'ID: {}", id);
        
        UserResponseDTO user = userService.getUserById(id);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Récupère un utilisateur par son email
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        log.info("Requête de récupération de l'utilisateur avec l'email: {}", email);
        
        UserResponseDTO user = userService.getUserByEmail(email);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Récupère un utilisateur par son username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        log.info("Requête de récupération de l'utilisateur avec le username: {}", username);
        
        UserResponseDTO user = userService.getUserByUsername(username);
        
        return ResponseEntity.ok(user);
    }

    /**
     * Met à jour un utilisateur existant
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        
        log.info("Requête de mise à jour de l'utilisateur avec l'ID: {}", id);
        
        UserResponseDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Supprime un utilisateur
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        log.info("Requête de suppression de l'utilisateur avec l'ID: {}", id);
        
        userService.deleteUser(id);
        
        return ResponseEntity.noContent().build();
    }
}
