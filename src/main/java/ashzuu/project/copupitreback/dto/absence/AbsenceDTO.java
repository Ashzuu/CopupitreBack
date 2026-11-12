package ashzuu.project.copupitreback.dto.absence;

import ashzuu.project.copupitreback.jpa.entities.organization.AbsenceStatus;
import ashzuu.project.copupitreback.jpa.entities.organization.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceDTO {
    private Long id;
    private Long organizationId;
    private Long musicianId;
    private String musicianFirstName;
    private String musicianLastName;
    private String musicianEmail;
    private String instrument;
    private EventType eventType;
    private LocalDate eventDate;
    private LocalDate endDate;
    private String reason;
    private AbsenceStatus status;
    private boolean submittedByAdmin;
    private Instant createdAt;
}
