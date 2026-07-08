package ashzuu.project.copupitreback.jpa.entities.organization;

import ashzuu.project.copupitreback.jpa.entities.*;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDateId;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnnualNeeds> annualNeeds = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Concert> concerts = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipmentRequest> equipmentRequests = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganizationAdmin> organizationAdmins = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RehearsalManual> rehearsalManuals = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RehearsalSchedule> rehearsalSchedules = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReinforcementRequest> reinforcementRequests = new ArrayList<>();

    public void addConcert(Concert concert){
        concert.setOrganization(this);
        this.concerts.add(concert);
    }

    public void addAnnualNeeds(AnnualNeeds annualNeeds){
        annualNeeds.setOrganization(this);
        this.annualNeeds.add(annualNeeds);
    }

    public void addAdmin(OrganizationAdmin admin){
        admin.setOrganization(this);
        this.organizationAdmins.add(admin);
    }

    public void addRehersalManual(RehearsalManual manual){
        manual.setOrganization(this);
        this.rehearsalManuals.add(manual);
    }

    public void addSchedules(RehearsalSchedule schedule){
        schedule.setOrganization(this);
        this.rehearsalSchedules.add(schedule);
    }

    public void addReinforcement(ReinforcementRequest request){
        request.setOrganization(this);
        this.reinforcementRequests.add(request);
    }

    public void addEquipment(EquipmentRequest request){
        request.setOrganization(this);
        this.equipmentRequests.add(request);
    }
}