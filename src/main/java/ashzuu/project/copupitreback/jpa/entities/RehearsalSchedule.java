package ashzuu.project.copupitreback.jpa.entities;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;

/**
 * Entité représentant le planning récurrent des répétitions d'une organisation.
 * <p>
 * Rôle : Permet de définir les jours habituels de la semaine (ex: tous les Jeudis de 20h à 22h) où l'orchestre se réunit.
 * <p>
 * Liens : ManyToOne avec {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} (l'orchestre concerné par ce planning).
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rehearsal_schedules", schema = "main")
public class RehearsalSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "day_of_week", nullable = false, length = 50)
    private String dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "location")
    private String location;


}