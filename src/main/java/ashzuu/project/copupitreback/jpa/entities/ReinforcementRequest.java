package ashzuu.project.copupitreback.jpa.entities;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une annonce ou une demande de renfort publiée par une organisation.
 * <p>
 * Rôle : Permet à un orchestre d'exprimer un besoin temporaire en musiciens (ex: recherche un tromboniste pour un concert précis).
 * <p>
 * Liens : 
 * - ManyToOne avec {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} (l'organisation émettrice).
 * - Cette entité est la cible des candidatures via l'entité {@link Application}.
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reinforcement_requests", schema = "main")
public class ReinforcementRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "title")
    private String title;

    @Column(name = "instrument_needed", nullable = false)
    private String instrumentNeeded;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @Column(name = "event_date")
    private Instant eventDate;

    @Column(name = "event_location")
    private String eventLocation;

    @Column(name = "repertoire_details", length = Integer.MAX_VALUE)
    private String repertoireDetails;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "deadline")
    private Instant deadline;

    @ColumnDefault("'OPEN'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @NonNull
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();
}