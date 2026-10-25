package ashzuu.project.copupitreback.jpa.entities;

import ashzuu.project.copupitreback.jpa.entities.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Entité représentant la candidature d'un musicien à une demande de renfort.
 * <p>
 * Rôle : Fait le pont entre un utilisateur (qui postule) et une annonce de renfort (le besoin d'un orchestre).
 * Elle gère l'état d'avancement de la réponse (PENDING, ACCEPTED, REJECTED).
 * </p>
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "applications", schema = "main")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "request_id")
    private ReinforcementRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "message", length = 1000)
    private String message;
}
