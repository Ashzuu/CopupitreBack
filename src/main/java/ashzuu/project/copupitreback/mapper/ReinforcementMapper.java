package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Composant responsable de la conversion entre les annonces de renfort, les candidatures et leurs DTOs respectifs.
 * <p>
 * But : Séparer la logique de comptage (ex: nombre de musiciens validés) et de conversion pour servir une vue prête à l'emploi au front-end.
 * <p>
 * Liens : Manipule {@link ReinforcementRequest} et {@link Application} pour produire {@link ReinforcementAnnouncementDTO} et {@link ReinforcementResponseDTO}.
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
                .requiredQuantity(dto.getRequiredQuantity())
                .eventDate(dto.getEventDate())
                .eventLocation(dto.getEventLocation())
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
        return ReinforcementResponseDTO.builder()
                .id(app.getId())
                .announcementId(app.getRequest() != null ? app.getRequest().getId() : null)
                .announcementTitle(app.getRequest() != null ? app.getRequest().getTitle() : null)
                .respondentUsername(app.getUser() != null ? app.getUser().getUsername() : null)
                .respondedAt(app.getAppliedAt())
                .status(app.getStatus())
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
        return app;
    }
}
