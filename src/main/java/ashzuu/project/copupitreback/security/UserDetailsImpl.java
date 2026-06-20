package ashzuu.project.copupitreback.security;

import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * <h1>UserDetailsImpl</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> C'est une implémentation de l'interface UserDetails de Spring Security. Elle enveloppe notre entité User.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Spring Security a besoin d'un objet de type UserDetails pour effectuer les vérifications d'authentification et d'autorisation. Cette classe fait le pont entre notre modèle de données et le framework de sécurité.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle est liée à l'entité User et est utilisée par CustomUserDetailsService et JwtAuthenticationFilter.</p>
 */
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
