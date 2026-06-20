package ashzuu.project.copupitreback.jpa.entities;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Entité représentant un concert ou un évènement musical organisé par un ensemble.
 * <p>
 * Rôle : Permet de stocker et d'afficher les informations d'un évènement public ou privé (titre, date, lieu).
 * <p>
 * Liens : ManyToOne avec {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} (l'orchestre producteur du concert).
 */
@Getter
@Setter
@Entity
@Table(name = "concerts", schema = "main")
public class Concert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "event_date")
    private Instant eventDate;

    @Column(name = "location")
    private String location;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;


}