package ashzuu.project.copupitreback.dto.reinforcement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * DTO représentant la candidature d'un musicien en réponse à une annonce de renfort.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinforcementResponseDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("announcementId")
    private Long announcementId;

    @JsonProperty("announcementTitle")
    private String announcementTitle;

    @JsonProperty("respondentUsername")
    private String respondentUsername;

    @JsonProperty("respondentFullName")
    private String respondentFullName;

    @JsonProperty("respondentInstrument")
    private String respondentInstrument;

    @JsonProperty("respondentEmail")
    private String respondentEmail;

    @JsonProperty("respondedAt")
    private Instant respondedAt;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("organizationId")
    private Long organizationId;

    @JsonProperty("organizationName")
    private String organizationName;

    @JsonProperty("organizationEmail")
    private String organizationEmail;

    @JsonProperty("type")
    private String type;

    @JsonProperty("eventDate")
    private Instant eventDate;

    @JsonProperty("eventLocation")
    private String eventLocation;
}
