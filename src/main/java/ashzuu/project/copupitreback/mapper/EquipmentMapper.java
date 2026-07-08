package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Composant responsable de la conversion d'une demande de matériel et de ses dates associées en un objet logique.
 * <p>
 * But : Simplifier l'envoi des requêtes d'équipement vers le front-end en aplatissant les dates en une simple liste textuelle.
 * <p>
 * Liens : Utilise {@link EquipmentRequest} et {@link EquipmentRequestDate} pour instancier un {@link EquipmentRequestDTO}.
 */
public class EquipmentMapper {

    /**
     * Transforme les {@link EquipmentRequest} en {@link EquipmentRequestDTO} afin de communiquer via les différentes couches front / back
     * @param request Request a transformer
     * @return Le DTO correspondant à la donnée en entrée
     */
    public static EquipmentRequestDTO toDto(EquipmentRequest request) {
        return EquipmentRequestDTO.builder()
            .id(request.getId())
            .title(request.getTitle())
            .equipmentName(request.getEquipmentName())
            .status(request.getStatus())
            .duration(request.getDuration())
            .description(request.getDescription())
            .organizationId(request.getOrganization().getId())
            .organizationName(request.getOrganization().getName())
            .eventDates(request.getEquipmentRequestDates().stream()
                .map(d -> d.getId().getEventDate().toString())
                .collect(Collectors.toList()))
            .build();
    }

    public static List<EquipmentRequestDTO> toDto(List<EquipmentRequest> requests){
        return requests.stream().map(EquipmentMapper::toDto).toList();
    }
}
