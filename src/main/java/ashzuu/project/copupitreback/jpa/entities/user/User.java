package ashzuu.project.copupitreback.jpa.entities.user;

import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
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
 * Entité principale représentant un utilisateur de la plateforme (un musicien).
 * <p>
 * Rôle : Stocke les informations personnelles, de connexion et musicales (instrument principal, années de pratique) du profil.
 * <p>
 * Liens : 
 * - Peut administrer des orchestres via {@link ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin}.
 * - Peut postuler à des renforts via {@link ashzuu.project.copupitreback.jpa.entities.Application}.
 * - Possède des instruments secondaires via {@link UserSecondaryInstrument}.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users", schema = "main")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "main_instrument")
    private String mainInstrument;

    @Column(name = "musical_description", length = Integer.MAX_VALUE)
    private String musicalDescription;

    @Column(name = "years_of_practice")
    private Integer yearsOfPractice;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @ColumnDefault("'USER'")
    private Role role = Role.USER;

    @NonNull
    @OneToMany(mappedBy = "user")
    private List<Application> applications = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "user")
    private List<OrganizationAdmin> organizationAdmins = new ArrayList<>();

    @NonNull
    @OneToMany(mappedBy = "user")
    private List<UserSecondaryInstrument> userSecondaryInstruments = new ArrayList<>();


}