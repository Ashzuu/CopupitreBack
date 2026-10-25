package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
import ashzuu.project.copupitreback.jpa.entities.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant responsable de la conversion entre les annonces de renfort, les candidatures et leurs DTOs respectifs.
 */
public class ReinforcementMapper {

    public static ReinforcementAnnouncementDTO toAnnouncementDto(ReinforcementRequest request) {
        if (request == null) {
            return null;
        }

        List<ReinforcementResponseDTO> responses = request.getApplications() != null ?
                request.getApplications().stream().map(ReinforcementMapper::toResponseDto).toList() : new ArrayList<>();

        long validated = responses.stream()
                .filter(r -> "ACCEPTED".equalsIgnoreCase(r.getStatus()))
                .count();

        return ReinforcementAnnouncementDTO.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .createdAt(request.getCreatedAt())
                .requiredQuantity(request.getRequiredQuantity())
                .eventLocation(request.getEventLocation())
                .eventDate(request.getEventDate())
                .repertoireDetails(request.getRepertoireDetails())
                .instrumentNeeded(request.getInstrumentNeeded())
                .organizationName(request.getOrganization() != null ? request.getOrganization().getName() : null)
                .organizationId(request.getOrganization() != null ? request.getOrganization().getId() : null)
                .status(request.getStatus())
                .validatedCount((int) validated)
                .responses(responses)
                .build();
    }

    public static List<ReinforcementAnnouncementDTO> toAnnouncementDto(List<ReinforcementRequest> requests){
        return requests.stream().map(ReinforcementMapper::toAnnouncementDto).toList();
    }

    public static ReinforcementRequest toEntity(ReinforcementAnnouncementDTO dto) {
        if (dto == null) {
            return null;
        }
        return ReinforcementRequest.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .instrumentNeeded(dto.getInstrumentNeeded())
                .requiredQuantity(dto.getRequiredQuantity() != null ? dto.getRequiredQuantity() : dto.getRequiredCount())
                .eventDate(dto.getEventDate())
                .eventLocation(dto.getEventLocation() != null ? dto.getEventLocation() : dto.getLocation())
                .repertoireDetails(dto.getRepertoireDetails())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .applications(new ArrayList<>())
                .build();
    }

    public static ReinforcementResponseDTO toResponseDto(Application app) {
        if (app == null) {
            return null;
        }

        String fullName = null;
        String instrument = null;
        String username = null;
        String email = null;
        if (app.getUser() != null) {
            username = app.getUser().getUsername();
            String first = app.getUser().getFirstName() != null ? app.getUser().getFirstName().trim() : "";
            String last = app.getUser().getLastName() != null ? app.getUser().getLastName().trim() : "";
            fullName = (first + " " + last).trim();
            if (fullName.isEmpty()) {
                fullName = username;
            }
            instrument = app.getUser().getMainInstrument();
            email = app.getUser().getEmail();
        }

        ReinforcementRequest req = app.getRequest();
        Long orgId = null;
        String orgName = null;
        String orgEmail = null;
        Instant eventDate = null;
        String eventLocation = null;

        if (req != null) {
            eventDate = req.getEventDate();
            eventLocation = req.getEventLocation();
            if (req.getOrganization() != null) {
                Organization org = req.getOrganization();
                orgId = org.getId();
                orgName = org.getName();
                if (org.getOrganizationAdmins() != null) {
                    for (OrganizationAdmin admin : org.getOrganizationAdmins()) {
                        if (admin.getUser() != null && admin.getUser().getEmail() != null) {
                            orgEmail = admin.getUser().getEmail();
                            break;
                        }
                    }
                }
            }
        }

        return ReinforcementResponseDTO.builder()
                .id(app.getId())
                .announcementId(req != null ? req.getId() : null)
                .announcementTitle(req != null ? req.getTitle() : null)
                .respondentUsername(username)
                .respondentFullName(fullName)
                .respondentInstrument(instrument)
                .respondentEmail(email)
                .respondedAt(app.getAppliedAt())
                .status(app.getStatus())
                .message(app.getMessage())
                .organizationId(orgId)
                .organizationName(orgName)
                .organizationEmail(orgEmail)
                .type("RENFORT")
                .eventDate(eventDate)
                .eventLocation(eventLocation)
                .build();
    }

    public static List<ReinforcementResponseDTO> toResponseDto(List<Application> apps){
        return apps.stream().map(ReinforcementMapper::toResponseDto).toList();
    }

    public static Application toEntity(ReinforcementResponseDTO dto) {
        if (dto == null) {
            return null;
        }
        Application app = new Application();
        app.setId(dto.getId());
        app.setStatus(dto.getStatus());
        app.setAppliedAt(dto.getRespondedAt());
        app.setMessage(dto.getMessage());
        return app;
    }
}
