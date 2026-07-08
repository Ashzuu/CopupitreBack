package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO représentant un besoin annuel en instrumentistes pour une saison.
 * <p>
 * But : Informer publiquement des besoins de recrutement globaux de l'orchestre.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.AnnualNeeds} et intégré au sein de {@link OrganizationDTO}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnualNeedDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String instrument;
    @JsonProperty()
    private Integer requiredCount;
    @JsonProperty()
    private String description;
}
