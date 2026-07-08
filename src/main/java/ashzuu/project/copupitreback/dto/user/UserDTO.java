package ashzuu.project.copupitreback.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO représentant le profil complet d'un utilisateur.
 * <p>
 * But : Transférer les données du musicien vers le front-end sans exposer les données sensibles (comme le mot de passe).
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.user.User} et complété par les {@link ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrument}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String username;
    @JsonProperty()
    private String firstName;
    @JsonProperty()
    private String lastName;
    @JsonProperty()
    private String mainInstrument;
    @JsonProperty()
    private List<String> secondaryInstruments;
    @JsonProperty()
    private String musicalDescription;
    @JsonProperty()
    private Integer yearsOfPractice;
}
