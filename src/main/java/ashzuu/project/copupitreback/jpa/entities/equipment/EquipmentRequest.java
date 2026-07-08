package ashzuu.project.copupitreback.jpa.entities.equipment;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité représentant une demande de prêt ou de mise à disposition de matériel (équipement).
 * <p>
 * Rôle : Permet à un orchestre de recenser le matériel nécessaire (ex: "Timbales", "Chaises") et d'en demander l'accès.
 * <p>
 * Liens : ManyToOne avec {@link ashzuu.project.copupitreback.jpa.entities.organization.Organization} (l'orchestre qui exprime le besoin en matériel).
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "equipment_requests", schema = "main")
public class EquipmentRequest {
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

    @Column(name = "equipment_name", nullable = false)
    private String equipmentName;

    @Column(name = "duration")
    private String duration;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @ColumnDefault("'OPEN'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @NonNull
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipmentRequestDate> equipmentRequestDates = new ArrayList<EquipmentRequestDate>();


}