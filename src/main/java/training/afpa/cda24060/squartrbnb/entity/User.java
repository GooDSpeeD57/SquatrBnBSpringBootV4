package training.afpa.cda24060.squartrbnb.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "username", nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 50)
    String username;

    @Column(name = "nom", nullable = false)
    @NotBlank
    @Size(max = 100)
    String nom;

    @Column(name = "prenom", nullable = false)
    @NotBlank
    @Size(max = 100)
    String prenom;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank
    @Email
    String email;

    @Column(name = "date_naissance", nullable = false)
    @NotNull
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateNaissance;

    @Nullable
    @Column(name = "photo_path")
    String photoPath;

    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    String password;

    @Column(name = "remember_token")
    @JsonIgnore
    String rememberToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @NotNull
    Role role;
}
