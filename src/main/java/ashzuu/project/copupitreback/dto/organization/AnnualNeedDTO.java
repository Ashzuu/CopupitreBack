package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * DTO représentant un besoin annuel en instrumentistes pour une saison.
 * <p>
 * But : Informer publiquement des besoins de recrutement globaux de l'orchestre.
 * <p>
 * Liens : Mappé depuis l'entité {@link ashzuu.project.copupitreback.jpa.entities.AnnualNeeds} et intégré au sein de {@link OrganizationDTO}.
 */
@Data
@Builder
public class AnnualNeedDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String instrument;
    @JsonProperty()
    private Integer count;
    @JsonProperty()
    private String description;
}
