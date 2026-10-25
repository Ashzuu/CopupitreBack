package ashzuu.project.copupitreback.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la mise à jour du statut d'une demande de matériel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEquipmentStatusRequest {
    @NotBlank(message = "Le statut est obligatoire")
    private String status;
}
