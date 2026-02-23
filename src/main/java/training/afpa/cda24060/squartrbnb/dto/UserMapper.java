package training.afpa.cda24060.squartrbnb.dto;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import training.afpa.cda24060.squartrbnb.entity.Role;
import training.afpa.cda24060.squartrbnb.entity.User;

/**
 * Mapper pour convertir entre User et UserDTO.
 *
 * ✅ CORRIGÉ Spring Boot 4 / JSpecify:
 * - Remplacement de lombok.NonNull par org.jspecify.annotations.NonNull
 *   (Spring Boot 4 a adopté JSpecify comme standard de null-safety,
 *    Spring's propre @NonNull est déprécié en faveur de JSpecify)
 * - La méthode toResponseDTO() ne peut PAS être @NonNull si elle retourne null
 *   quand user == null. Deux options : retirer le @NonNull ou lancer une exception.
 *   Ici on retire le @NonNull (le service ne devrait jamais passer null de toute façon).
 */
@Component
@NullMarked
public class UserMapper {

    /**
     * Convertit un User en UserResponseDTO.
     * ✅ CORRIGÉ: retiré @NonNull sur le type de retour car la méthode peut retourner null.
     *    En pratique le service garantit que user n'est jamais null ici.
     */
    public @Nullable UserResponseDTO toResponseDTO(@Nullable User user) {
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
     * Convertit un Role en RoleResponseDTO.
     */
    private UserResponseDTO.@Nullable RoleResponseDTO toRoleResponseDTO(@Nullable Role role) {
        if (role == null) {
            return null;
        }

        return UserResponseDTO.RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    /**
     * Convertit un UserCreateDTO en User.
     */
    public @Nullable User toEntity(@Nullable UserCreateDTO dto) {
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
     * Met à jour un User existant avec les données de UserUpdateDTO.
     */
    public void updateEntityFromDTO(@Nullable UserUpdateDTO dto, @Nullable User user) {
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
        // Le mot de passe est géré séparément dans le service
    }
}
