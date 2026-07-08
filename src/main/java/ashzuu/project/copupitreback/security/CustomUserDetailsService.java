package ashzuu.project.copupitreback.security;

import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service personnalisé pour charger les détails d'un utilisateur de l'application.
 * <p>
 * Ce service implémente l'interface {@link UserDetailsService} de Spring Security.
 * Il est utilisé par le système d'authentification pour valider et récupérer
 * les informations de l'utilisateur connecté à partir de son nom d'utilisateur (username).
 * </p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Dépôt permettant de communiquer avec la table des utilisateurs en base de données.
     */
    private final UserRepository userRepository;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param userRepository le dépôt des utilisateurs
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Charge les détails d'un utilisateur en utilisant son nom d'utilisateur.
     * <p>
     * Cette méthode est appelée en interne par Spring Security lors du processus d'authentification.
     * </p>
     *
     * @param username le nom d'utilisateur de la personne cherchant à s'authentifier
     * @return un objet {@link UserDetails} contenant les informations de l'utilisateur authentifié
     * @throws UsernameNotFoundException si aucun utilisateur correspondant à ce nom d'utilisateur n'est trouvé
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le nom d'utilisateur ou l'email : " + username));

        return new UserDetailsImpl(user);
    }
}
