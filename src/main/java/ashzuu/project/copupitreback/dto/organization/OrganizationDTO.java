package ashzuu.project.copupitreback.dto.organization;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

/**
 * DTO représentant une organisation musicale (orchestre) et tous ses éléments associés.
 * <p>
 * But : Fournir au front-end un objet complet pour afficher la page de l'organisation avec l'ensemble de ses plannings et besoins en une seule requête.
 * <p>
 * Liens : Mappé depuis l'entité racine {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} et contient d'autres DTOs enfants (ConcertDto, RehearsalScheduleDto, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
    @JsonProperty()
    private Long id;
    @JsonProperty()
    private String name;
    @JsonProperty()
    private String city;
    @JsonProperty()
    private String formationType;
    @JsonProperty()
    private String description;
    @JsonProperty()
    private String concertDress;
    @JsonProperty()
    private String logisticsInfo;
    @JsonProperty()
    private Integer musicianCount;
    @JsonProperty()
    private String rehearsalAddress;
    @JsonProperty()
    private String rehearsalSchedule;
    @JsonProperty()
    private Instant createdAt;
    @JsonProperty()
    private List<RehearsalScheduleDTO> rehearsalSchedules;
    @JsonProperty()
    private List<String> adminUsernames;
    @JsonProperty()
    private List<ConcertDTO> concerts;
    @JsonProperty()
    private List<AnnualNeedDTO> annualNeeds;
    @JsonProperty()
    private List<RehearsalManualDTO> rehearsalManuals;
    @JsonProperty()
    private List<EquipmentRequestDTO> equipmentRequests;
    @JsonProperty()
    private List<ReinforcementAnnouncementDTO> reinforcementRequests;
}
