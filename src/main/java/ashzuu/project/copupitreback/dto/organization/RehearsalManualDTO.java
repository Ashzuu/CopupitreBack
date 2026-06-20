package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO représentant une date de répétition exceptionnelle rajoutée manuellement.
 * <p>
 * But : Transmettre les informations des répétitions ponctuelles (raccords, répétitions générales) sortant du planning classique.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.RehearsalManual} et intégré au sein de {@link OrganizationDTO}.
 */
@Data
@Builder
public class RehearsalManualDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private LocalDate date;
    @JsonProperty()
    private LocalTime startTime;
    @JsonProperty()
    private LocalTime endTime;
    @JsonProperty()
    private String location;
}
