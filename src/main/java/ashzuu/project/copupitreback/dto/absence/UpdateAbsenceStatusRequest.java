package ashzuu.project.copupitreback.dto.absence;

import ashzuu.project.copupitreback.jpa.entities.organization.AbsenceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAbsenceStatusRequest {
    @NotNull(message = "Le statut de l'absence est obligatoire")
    private AbsenceStatus status;
}
