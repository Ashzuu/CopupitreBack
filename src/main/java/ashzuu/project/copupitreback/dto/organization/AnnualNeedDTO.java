package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonAlias;
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
 * Assure la compatibilité bidirectionnelle entre 'requiredCount' et 'count'.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnualNeedDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("instrument")
    private String instrument;

    @JsonProperty("requiredCount")
    @JsonAlias({"count", "requiredCount"})
    private Integer requiredCount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("count")
    public Integer getCount() {
        return this.requiredCount;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        if (count != null) {
            this.requiredCount = count;
        }
    }

    public static class AnnualNeedDTOBuilder {
        public AnnualNeedDTOBuilder count(Integer count) {
            this.requiredCount = count;
            return this;
        }
    }
}
