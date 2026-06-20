package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;

/**
 * Composant responsable de la conversion entre les annonces de renfort, les candidatures et leurs DTOs respectifs.
 * <p>
 * But : Séparer la logique de comptage (ex: nombre de musiciens validés) et de conversion pour servir une vue prête à l'emploi au front-end.
 * <p>
 * Liens : Manipule {@link ReinforcementRequest} et {@link Application} pour produire {@link ReinforcementAnnouncementDTO} et {@link ReinforcementResponseDTO}.
 */
public class ReinforcementMapper {
    public static ReinforcementAnnouncementDTO toAnnouncementDto(ReinforcementRequest request) {
        return ReinforcementAnnouncementDTO.builder().build();
    }
}
