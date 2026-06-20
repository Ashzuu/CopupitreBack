package ashzuu.project.copupitreback.jpa.entities.organization;

import ashzuu.project.copupitreback.jpa.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Entité de jointure représentant les droits d'administration d'un utilisateur sur une organisation.
 * <p>
 * Rôle : Détermine quel utilisateur a le droit de modifier les informations de l'orchestre ou de gérer ses annonces.
 * <p>
 * Liens : 
 * - ManyToOne avec {@link ashzuu.project.copupitreback.jpa.entities.user.User} (l'administrateur).
 * - ManyToOne avec {@link Organization} (l'orchestre administré).
 * Utilise la clé composite {@link OrganizationAdminId}.
 */
@Getter
@Setter
@Entity
@Table(name = "organization_admins", schema = "main")
public class OrganizationAdmin {
    @EmbeddedId
    private OrganizationAdminId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("organizationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "added_at")
    private Instant addedAt;


}