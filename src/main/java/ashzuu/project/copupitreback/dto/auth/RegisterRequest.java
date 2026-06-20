package ashzuu.project.copupitreback.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * <h1>RegisterRequest</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Objet de transfert de données (DTO) contenant les informations envoyées par un client pour s'inscrire.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Valider et transporter les données d'inscription (nom d'utilisateur, e-mail, mot de passe) depuis le contrôleur jusqu'au service d'authentification.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle est utilisée par AuthController et AuthService.</p>
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Le format de l'email est invalide")
    private String email;

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom de famille est requis")
    private String lastName;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "L'instrument principal est requis")
    private String mainInstrument;

    private String secondaryInstruments;

    private String musicalDescription;

    private Integer yearsOfPractice;
}
