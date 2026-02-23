package training.afpa.cda24060.squartrbnb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO pour les réponses contenant les informations utilisateur
 * Ne contient PAS le mot de passe ni le remember token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Integer id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;
    
    private String photoPath;
    private RoleResponseDTO role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleResponseDTO {
        private Integer id;
        private String name;
    }
}
