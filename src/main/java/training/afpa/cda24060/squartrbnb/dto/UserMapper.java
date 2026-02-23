package training.afpa.cda24060.squartrbnb.dto;

import lombok.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import training.afpa.cda24060.squartrbnb.entity.Role;
import training.afpa.cda24060.squartrbnb.entity.User;
import org.jspecify.annotations.Nullable;

/**
 * Mapper pour convertir entre User et UserDTO
 */
@Component
@NullMarked
public class UserMapper {

    /**
     * Convertit un User en UserResponseDTO
     */
    public @NonNull UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .email(user.getEmail())
                .dateNaissance(user.getDateNaissance())
                .photoPath(user.getPhotoPath())
                .role(toRoleResponseDTO(user.getRole()))
                .build();
    }

    /**
     * Convertit un Role en RoleResponseDTO
     */
    private UserResponseDTO.RoleResponseDTO toRoleResponseDTO(Role role) {
        if (role == null) {
            return null;
        }

        return UserResponseDTO.RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    /**
     * Convertit un UserCreateDTO en User
     */
    public User toEntity(UserCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setDateNaissance(dto.getDateNaissance());
        user.setPhotoPath(dto.getPhotoPath());
        user.setPassword(dto.getPassword()); // Sera encodé par le service
        
        return user;
    }

    /**
     * Met à jour un User existant avec les données de UserUpdateDTO
     */
    public void updateEntityFromDTO(UserUpdateDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getNom() != null) {
            user.setNom(dto.getNom());
        }
        if (dto.getPrenom() != null) {
            user.setPrenom(dto.getPrenom());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getDateNaissance() != null) {
            user.setDateNaissance(dto.getDateNaissance());
        }
        if (dto.getPhotoPath() != null) {
            user.setPhotoPath(dto.getPhotoPath());
        }
        // Le mot de passe sera géré séparément dans le service
    }
}
