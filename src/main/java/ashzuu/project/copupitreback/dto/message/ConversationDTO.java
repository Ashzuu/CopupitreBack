package ashzuu.project.copupitreback.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO synthétique représentant une conversation de la messagerie interne (US-COM-03 & US-COM-04).
 * Strictement conforme à RG-COM-01 (aucune donnée personnelle email/téléphone).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String id;
    private String contextType; // "APPLICATION", "ANNUAL_APPLICATION", "EQUIPMENT"
    private Long contextId;
    private String title;
    private String subtitle;
    private String otherPartyName;
    private String otherPartyRole; // "Candidat", "Organisation", "Demandeur"
    private String lastMessage;
    private Instant lastMessageAt;
    private long messageCount;
    private String status; // "PENDING", "ACCEPTED", "REJECTED", "OPEN", "MET", etc.
}
