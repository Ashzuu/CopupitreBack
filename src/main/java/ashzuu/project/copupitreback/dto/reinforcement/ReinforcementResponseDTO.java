package ashzuu.project.copupitreback.dto.reinforcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * DTO représentant la candidature d'un musicien en réponse à une annonce de renfort.
 * <p>
 * But : Fournir au créateur de l'annonce les détails sur les musiciens ayant postulé et l'état actuel de leur candidature.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.Application} et est rattaché à un {@link ReinforcementAnnouncementDTO}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinforcementResponseDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private Long announcementId;
    @JsonProperty()
    private String announcementTitle;
    @JsonProperty()
    private String respondentUsername;
    @JsonProperty()
    private Instant respondedAt;
    @JsonProperty()
    private String status;
}
