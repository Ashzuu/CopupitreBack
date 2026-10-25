package ashzuu.project.copupitreback.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO pour un message de fil de discussion contextuel (US-COM-03 & US-COM-04).
 * Conforme à RG-COM-01 : ne divulgue jamais l'adresse email ou le numéro de téléphone des participants.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String contextType;
    private Long contextId;
    private String senderUsername;
    private String senderFullName;
    private String senderRole; // CANDIDATE, ADMIN, ORGANIZER

    @JsonProperty("isMe")
    private boolean isMe;

    private String content;
    private Instant createdAt;
}
