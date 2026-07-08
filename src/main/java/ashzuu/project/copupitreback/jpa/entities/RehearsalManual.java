package ashzuu.project.copupitreback.jpa.entities;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entité représentant une répétition spécifique rajoutée manuellement au calendrier.
 * <p>
 * Rôle : Gère les dates de répétitions qui sont ponctuelles, uniques ou qui sortent du planning régulier de l'orchestre.
 * <p>
 * Liens : ManyToOne avec {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} (l'orchestre concerné).
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rehearsal_manuals", schema = "main")
public class RehearsalManual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "manual_date", nullable = false)
    private LocalDate manualDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "location")
    private String location;


}