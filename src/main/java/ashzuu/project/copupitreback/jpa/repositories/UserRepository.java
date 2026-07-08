package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    /**
     * Recherche un utilisateur par son nom d'utilisateur ou par son adresse email.
     *
     * @param username le nom d'utilisateur à rechercher
     * @param email    l'adresse email à rechercher
     * @return un {@link Optional} contenant l'utilisateur s'il est trouvé, ou vide sinon
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
