package ashzuu.project.copupitreback.dto.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête pour ajouter un administrateur à une organisation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAdminRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @JsonProperty("username")
    private String username;
}
