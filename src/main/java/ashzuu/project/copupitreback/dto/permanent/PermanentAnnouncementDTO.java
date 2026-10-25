package ashzuu.project.copupitreback.dto.permanent;

import ashzuu.project.copupitreback.dto.organization.RehearsalScheduleDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO représentant une annonce de recrutement de musicien permanent (besoin annuel de la saison).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermanentAnnouncementDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("organizationId")
    private Long organizationId;

    @JsonProperty("organizationName")
    private String organizationName;

    @JsonProperty("organizationCity")
    private String organizationCity;

    @JsonProperty("formationType")
    private String formationType;

    @JsonProperty("musicianCount")
    private Integer musicianCount;

    @JsonProperty("organizationDescription")
    private String organizationDescription;

    @JsonProperty("rehearsalAddress")
    private String rehearsalAddress;

    @JsonProperty("rehearsalSchedule")
    private String rehearsalSchedule;

    @JsonProperty("regularSchedules")
    private List<RehearsalScheduleDTO> regularSchedules;

    @JsonProperty("adminUsernames")
    private List<String> adminUsernames;

    @JsonProperty("instrument")
    private String instrument;

    @JsonProperty("requiredCount")
    private Integer requiredCount;

    @JsonProperty("validatedCount")
    private Integer validatedCount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    @Builder.Default
    private String type = "PERMANENT";

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("responses")
    private List<ReinforcementResponseDTO> responses;
}
