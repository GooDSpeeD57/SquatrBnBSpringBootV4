package training.afpa.cda24060.squartrbnb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    @Size(min = 3, max = 50)
    private String username;

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String prenom;

    @Email
    private String email;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateNaissance;

    private String photoPath;

    @Size(min = 8)
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?]).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;

    private Integer roleId;
}
