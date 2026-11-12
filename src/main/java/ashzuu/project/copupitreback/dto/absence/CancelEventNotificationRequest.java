package ashzuu.project.copupitreback.dto.absence;

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
public class CancelEventNotificationRequest {
    @NotNull(message = "Le type d'événement est obligatoire")
    private EventType eventType;

    @NotNull(message = "La date de l'événement est obligatoire")
    private LocalDate eventDate;

    @NotBlank(message = "Le motif ou message d'annulation est obligatoire")
    private String message;
}
