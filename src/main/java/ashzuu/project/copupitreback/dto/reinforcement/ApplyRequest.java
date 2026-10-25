package ashzuu.project.copupitreback.dto.reinforcement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corps de requête pour soumettre une candidature à une annonce de renfort.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyRequest {
    private String message;
}
