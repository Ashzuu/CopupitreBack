package ashzuu.project.copupitreback.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <h1>LoginRequest</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Objet de transfert de données (DTO) contenant les identifiants envoyés par un client pour se connecter.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Transporter le nom d'utilisateur et le mot de passe depuis le contrôleur jusqu'au service d'authentification.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle est utilisée par AuthController.</p>
 */
@Data
public class LoginRequest {
    @NotBlank(message = "Le nom d'utilisateur est requis")
    private String username;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}
