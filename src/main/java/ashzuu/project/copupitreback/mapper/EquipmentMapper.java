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
        if (request == null) {
            return null;
        }
        return EquipmentRequestDTO.builder()
            .id(request.getId())
            .title(request.getTitle())
            .equipmentName(request.getEquipmentName())
            .status(request.getStatus())
            .duration(request.getDuration())
            .description(request.getDescription())
            .organizationId(request.getOrganization() != null ? request.getOrganization().getId() : null)
            .organizationName(request.getOrganization() != null ? request.getOrganization().getName() : null)
            .eventDates(request.getEquipmentRequestDates() != null ? request.getEquipmentRequestDates().stream()
                .map(d -> d.getId().getEventDate())
                .collect(Collectors.toList()) : List.of())
            .createdAt(request.getCreatedAt())
            .build();
    }

    public static List<EquipmentRequestDTO> toDto(List<EquipmentRequest> requests){
        return requests.stream().map(EquipmentMapper::toDto).toList();
    }

    public static EquipmentRequest toEntity(EquipmentRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return EquipmentRequest.builder()
            .id(dto.getId())
            .title(dto.getTitle())
            .equipmentName(dto.getEquipmentName())
            .duration(dto.getDuration())
            .description(dto.getDescription())
            .status(dto.getStatus())
            .createdAt(dto.getCreatedAt())
            .build();
    }
}
