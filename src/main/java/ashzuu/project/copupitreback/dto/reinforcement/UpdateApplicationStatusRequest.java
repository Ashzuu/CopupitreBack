package ashzuu.project.copupitreback.dto.reinforcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corps de requête pour valider ou refuser une candidature à une annonce de renfort.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationStatusRequest {

    @NotBlank(message = "Le statut est obligatoire")
    @Pattern(regexp = "ACCEPTED|REJECTED", message = "Le statut doit être ACCEPTED ou REJECTED")
    private String status;
}
