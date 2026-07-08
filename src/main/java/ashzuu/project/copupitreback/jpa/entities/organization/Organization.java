package ashzuu.project.copupitreback.jpa.entities.organization;

import ashzuu.project.copupitreback.jpa.entities.*;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité principale représentant une organisation musicale (orchestre, chorale, harmonie, etc.).
 * <p>
 * Rôle : Contient les détails identitaires et logistiques de la formation (nom, ville, effectif, type de formation).
 * <p>
 * Liens : 
 * - Sert de racine à de nombreuses autres entités telles que {@link Concert},
 *   {@link AnnualNeeds}, {@link ReinforcementRequest}, etc.
 * - Est gérée par des utilisateurs administrateurs via {@link OrganizationAdmin}.
 */
@Entity
@Data
@Table(name = "organizations", schema = "main")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "formation_type")
    private String formationType;

    @Column(name = "city")
    private String city;

    @Column(name = "rehearsal_address")
    private String rehearsalAddress;

    @Column(name = "rehearsal_schedule")
    private String rehearsalSchedule;

    @Column(name = "logistics_info", length = Integer.MAX_VALUE)
    private String logisticsInfo;

    @Column(name = "concert_dress", length = Integer.MAX_VALUE)
    private String concertDress;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "musician_count")
    private Integer musicianCount;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<AnnualNeeds> annualNeeds = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<Concert> concerts = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<EquipmentRequest> equipmentRequests = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<OrganizationAdmin> organizationAdmins = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<RehearsalManual> rehearsalManuals = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<RehearsalSchedule> rehearsalSchedules = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization")
    private List<ReinforcementRequest> reinforcementRequests = new ArrayList<>();


}