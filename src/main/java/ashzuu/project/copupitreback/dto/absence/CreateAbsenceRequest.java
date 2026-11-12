package ashzuu.project.copupitreback.dto.absence;

import ashzuu.project.copupitreback.jpa.entities.organization.AbsenceStatus;
import ashzuu.project.copupitreback.jpa.entities.organization.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAbsenceRequest {
    @NotNull(message = "L'identifiant du musicien est obligatoire")
    private Long musicianId;

    @NotNull(message = "Le type d'événement est obligatoire")
    private EventType eventType;

    @NotNull(message = "La date de l'événement est obligatoire")
    private LocalDate eventDate;

    private LocalDate endDate;

    @NotBlank(message = "Le motif de l'absence est obligatoire")
    private String reason;

    /**
     * Statut de l'absence spécifié par un administrateur (JUSTIFIED ou UNJUSTIFIED).
     * Ignoré si soumis par un musicien standard (automatiquement PENDING ou UNJUSTIFIED selon le délai).
     */
    private AbsenceStatus status;
}
