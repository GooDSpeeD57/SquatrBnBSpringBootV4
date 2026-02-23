package training.afpa.cda24060.squartrbnb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import training.afpa.cda24060.squartrbnb.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Méthode pour vérifier si un email existe déjà
    boolean existsByEmail(String email);
    
    // Méthode pour vérifier si un username existe déjà
    boolean existsByUsername(String username);
    
    // Méthode pour rechercher un utilisateur par email
    Optional<User> findByEmail(String email);
    
    // Méthode pour rechercher un utilisateur par username
    Optional<User> findByUsername(String username);
}
