package ashzuu.project.copupitreback.jpa.entities;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant les besoins annuels en effectifs pour une organisation.
 * <p>
 * Rôle : Permet à un orchestre de définir quel instrument est recherché et en quelle quantité pour la saison en cours.
 * <p>
 * Liens : Associée à l'entité {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} via une relation ManyToOne (plusieurs besoins pour une organisation).
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="annual_needs", schema = "main")
public class AnnualNeeds {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Column(name = "instrument", nullable = false)
    private String instrument;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "annualNeed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AnnualNeedApplication> applications = new ArrayList<>();
}
