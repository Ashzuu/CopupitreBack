package ashzuu.project.copupitreback.jpa.entities.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Entité représentant un instrument secondaire joué par un utilisateur.
 * <p>
 * Rôle : Permet de gérer la multiplicité des instruments maîtrisés par un musicien en plus de son instrument principal.
 * <p>
 * Liens : ManyToOne avec {@link User} (le musicien propriétaire de ces compétences).
 * Utilise la clé composite {@link UserSecondaryInstrumentId}.
 */
@Getter
@Setter
@Entity
@Table(name = "user_secondary_instruments", schema = "main")
public class UserSecondaryInstrument {
    @EmbeddedId
    private UserSecondaryInstrumentId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}