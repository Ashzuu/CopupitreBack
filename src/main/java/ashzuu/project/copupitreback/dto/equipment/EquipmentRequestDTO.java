package ashzuu.project.copupitreback.dto.equipment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO représentant une demande de prêt de matériel inter-orchestre.
 * <p>
 * But : Fournir une vue simplifiée d'une demande de prêt, incluant les dates exactes sous forme de liste de chaînes de caractères.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest} en aggrégeant les {@link ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDate}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequestDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String title;
    @JsonProperty()
    private Long organizationId;
    @JsonProperty()
    private String organizationName;
    @JsonProperty()
    private String equipmentName;
    @JsonProperty()
    private String duration;
    @JsonProperty()
    private List<LocalDate> eventDates;
    @JsonProperty()
    private String status;
    @JsonProperty()
    private String description;
    @JsonProperty()
    private Instant createdAt;
}
