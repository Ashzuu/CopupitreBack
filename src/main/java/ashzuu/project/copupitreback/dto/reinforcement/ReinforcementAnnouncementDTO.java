package ashzuu.project.copupitreback.dto.reinforcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

/**
 * DTO représentant une annonce de recherche de renfort pour un concert spécifique.
 * <p>
 * But : Transférer les informations d'une annonce ponctuelle (l'instrument manquant, le nombre requis et le nombre déjà validé) pour l'affichage sur la plateforme.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest} et contient une liste de {@link ReinforcementResponseDTO}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinforcementAnnouncementDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String title;
    @JsonProperty()
    private Long organizationId;
    @JsonProperty()
    private String organizationName;
    @JsonProperty()
    private String instrumentNeeded;
    @JsonProperty()
    private Instant eventDate;
    @JsonProperty()
    private Instant deadline;
    @JsonProperty()
    private String eventLocation;
    @JsonProperty()
    private String repertoireDetails;
    @JsonProperty()
    private String description;
    @JsonProperty()
    private Integer requiredQuantity;
    @JsonProperty()
    private Integer validatedCount;
    @JsonProperty()
    private String status;
    @JsonProperty()
    private Instant createdAt;
    @JsonProperty()
    private List<ReinforcementResponseDTO> responses;
}
