package ashzuu.project.copupitreback.dto.reinforcement;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

/**
 * DTO représentant une annonce de recherche de renfort pour un concert ou projet spécifique.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReinforcementAnnouncementDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("organizationId")
    private Long organizationId;

    @JsonProperty("organizationName")
    private String organizationName;

    @JsonProperty("instrumentNeeded")
    private String instrumentNeeded;

    @JsonProperty("eventDate")
    private Instant eventDate;

    @JsonProperty("deadline")
    private Instant deadline;

    @JsonProperty("eventLocation")
    @JsonAlias("location")
    private String eventLocation;

    @JsonProperty("repertoireDetails")
    private String repertoireDetails;

    @JsonProperty("description")
    private String description;

    @JsonProperty("requiredQuantity")
    @JsonAlias("requiredCount")
    private Integer requiredQuantity;

    @JsonProperty("validatedCount")
    private Integer validatedCount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("responses")
    private List<ReinforcementResponseDTO> responses;

    @JsonProperty("location")
    public String getLocation() {
        return eventLocation;
    }

    @JsonProperty("location")
    public void setLocation(String location) {
        this.eventLocation = location;
    }

    @JsonProperty("requiredCount")
    public Integer getRequiredCount() {
        return requiredQuantity;
    }

    @JsonProperty("requiredCount")
    public void setRequiredCount(Integer requiredCount) {
        this.requiredQuantity = requiredCount;
    }
}
