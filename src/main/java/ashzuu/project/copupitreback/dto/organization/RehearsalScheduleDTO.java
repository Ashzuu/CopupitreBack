package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * DTO représentant une entrée du planning régulier des répétitions.
 * <p>
 * But : Transmettre les jours et horaires récurrents d'un orchestre pour affichage côté front-end.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.RehearsalSchedule} et intégré au sein de {@link OrganizationDTO}.
 */
@Data
@Builder
public class RehearsalScheduleDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String dayOfWeek;
    @JsonProperty()
    private String startTime;
    @JsonProperty()
    private String endTime;
    @JsonProperty()
    private String location;
    @JsonProperty()
    private String organizationName;
}
