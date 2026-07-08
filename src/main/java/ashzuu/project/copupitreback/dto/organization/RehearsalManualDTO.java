package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class RehearsalManualDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private LocalDate manualDate;
    @JsonProperty()
    private LocalTime startTime;
    @JsonProperty()
    private LocalTime endTime;
    @JsonProperty()
    private String location;
}
