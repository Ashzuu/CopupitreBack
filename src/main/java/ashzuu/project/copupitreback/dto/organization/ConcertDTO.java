package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * DTO représentant un évènement musical ou un concert.
 * <p>
 * But : Permettre au front-end d'afficher la liste des prochains concerts de l'orchestre avec leurs détails.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.Concert} et intégré au sein de {@link OrganizationDTO}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String title;
    @JsonProperty()
    private Instant eventDate;
    @JsonProperty()
    private String location;
    @JsonProperty()
    private String description;
}
