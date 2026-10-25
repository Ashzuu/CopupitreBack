package ashzuu.project.copupitreback.dto.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO transportant les messages de journalisation ou d'erreurs émis depuis le frontend
 * afin d'éviter l'utilisation de console.error/console.log dans le navigateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientLogRequest {

    /** Niveau de journalisation : ERROR, WARN, INFO. */
    @NotBlank(message = "Le niveau de log est requis")
    @JsonProperty("level")
    private String level;

    /** Message informatif ou d'erreur. */
    @NotBlank(message = "Le message de log est requis")
    @Size(max = 2000, message = "Le message ne doit pas dépasser 2000 caractères")
    @JsonProperty("message")
    private String message;

    /** Contexte / nom du composant ou service émetteur. */
    @Size(max = 255, message = "Le contexte ne doit pas dépasser 255 caractères")
    @JsonProperty("context")
    private String context;

    /** URL de la page active côté client lors de l'événement. */
    @Size(max = 500, message = "L'URL ne doit pas dépasser 500 caractères")
    @JsonProperty("url")
    private String url;

    /** Trace de pile facultative pour diagnostic approfondi. */
    @Size(max = 3000, message = "La trace de pile ne doit pas dépasser 3000 caractères")
    @JsonProperty("stack")
    private String stack;

    /** Horodatage côté client. */
    @JsonProperty("timestamp")
    private Instant timestamp;
}
