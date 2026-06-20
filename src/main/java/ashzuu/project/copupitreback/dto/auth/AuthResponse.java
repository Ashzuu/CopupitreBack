package ashzuu.project.copupitreback.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>AuthResponse</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> DTO de réponse renvoyé au client après une authentification ou une inscription réussie.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Fournir le token JWT et les informations de base de l'utilisateur au client (Angular).</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle est renvoyée par AuthController et AuthService.</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String role;
}
