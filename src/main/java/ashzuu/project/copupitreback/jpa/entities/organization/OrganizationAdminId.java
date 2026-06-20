package ashzuu.project.copupitreback.jpa.entities.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Clé primaire composite pour l'entité {@link OrganizationAdmin}.
 * <p>
 * Rôle : Associe de manière unique l'identifiant d'un utilisateur (`userId`) à celui d'une organisation (`organizationId`).
 */
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class OrganizationAdminId implements Serializable {
    private static final long serialVersionUID = 1737311563520885476L;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
}