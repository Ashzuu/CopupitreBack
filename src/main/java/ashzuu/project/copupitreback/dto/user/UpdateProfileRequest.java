package ashzuu.project.copupitreback.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>UpdateProfileRequest</h1>
 * <p>DTO représentant les données envoyées pour mettre à jour le profil de l'utilisateur connecté.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Le prénom est requis")
    private String firstName;

    @NotBlank(message = "Le nom de famille est requis")
    private String lastName;

    @NotBlank(message = "L'instrument principal est requis")
    private String mainInstrument;

    private List<String> secondaryInstruments;

    private String musicalDescription;

    @PositiveOrZero(message = "Les années de pratique ne peuvent pas être négatives")
    private Integer yearsOfPractice;
}
