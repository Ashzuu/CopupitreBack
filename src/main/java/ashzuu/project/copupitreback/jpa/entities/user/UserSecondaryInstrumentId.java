package ashzuu.project.copupitreback.jpa.entities.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Clé primaire composite pour l'entité {@link UserSecondaryInstrument}.
 * <p>
 * Rôle : Identifie de façon unique la relation entre un utilisateur (`userId`) et le nom de l'instrument secondaire (`instrument`).
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class UserSecondaryInstrumentId implements Serializable {
    private static final long serialVersionUID = 6900069100173031373L;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "instrument", nullable = false)
    private String instrument;


}