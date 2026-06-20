package ashzuu.project.copupitreback.security;

import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * <h1>CustomUserDetailsService</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Implémentation du service UserDetailsService de Spring Security.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Permettre à Spring Security de récupérer les informations d'un utilisateur depuis la base de données à l'aide de son nom d'utilisateur.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle utilise UserRepository et retourne un UserDetailsImpl.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        return new UserDetailsImpl(user);
    }
}
