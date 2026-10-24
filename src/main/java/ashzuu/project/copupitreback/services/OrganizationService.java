package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.organization.*;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.*;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdminId;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.EquipmentMapper;
import ashzuu.project.copupitreback.mapper.OrganizationMapper;
import ashzuu.project.copupitreback.mapper.ReinforcementMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service gérant la logique métier liée aux organisations.
 * <p>
 * Ce service permet de gérer, lister et récupérer les informations relatives aux organisations (associations, orchestres, etc.)
 * ainsi que les plannings, concerts, besoins de recrutement, administrateurs et demandes associées.
 * </p>
 */
@Service
public class OrganizationService {

    /**
     * Dépôt permettant de communiquer avec la table des organisations en base de données.
     */
    private final OrganizationRepository repo;

    /**
     * Dépôt permettant de communiquer avec la table des utilisateurs en base de données.
     */
    private final UserRepository userRepository;

    /**
     * Service de notification email.
     */
    private final EmailService emailService;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param repo           le dépôt des organisations
     * @param userRepository le dépôt des utilisateurs
     * @param emailService   le service d'envoi d'emails
     */
    public OrganizationService(OrganizationRepository repo, UserRepository userRepository, EmailService emailService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Récupère toutes les organisations de l'application sous forme de DTO.
     *
     * @return la liste de toutes les organisations sous forme de {@link OrganizationDTO}
     */
    public List<OrganizationDTO> getAll() {
        return OrganizationMapper.toDto(this.repo.findAll());
    }

    public OrganizationDTO getById(String id) {
        Optional<Organization> org = this.repo.findById(Long.parseLong(id));
        if (org.isEmpty()) {
            throw new UnknownID("L'organisation recherchée est inconnue");
        }
        return OrganizationMapper.toDto(org.get());
    }

    /**
     * Récupère les organisations associées à un utilisateur spécifique.
     *
     * @param details les détails de l'utilisateur connecté contenant son identifiant
     * @return la liste des organisations associées à cet utilisateur sous forme de {@link OrganizationDTO}
     */
    public List<OrganizationDTO> getOrganizationFromUser(UserDetailsImpl details) {
        return OrganizationMapper.toDto(this.repo.getAllByUserId(details.getId()));
    }

    /**
     * Récupère la liste des annonces de renfort publiées pour une organisation spécifique.
     *
     * @param id l'identifiant unique de l'organisation
     * @return la liste des annonces de renfort liées à l'organisation
     */
    public List<ReinforcementAnnouncementDTO> getAnnouncementsOnOrganization(String id) {
        Optional<Organization> org = this.repo.findById(Long.parseLong(id));
        if (org.isEmpty()) {
            throw new UnknownID("L'organisation recherchée est inconnue");
        }
        return ReinforcementMapper.toAnnouncementDto(org.get().getReinforcementRequests());
    }

    /**
     * Récupère la liste des demandes d'équipements émises pour une organisation spécifique.
     *
     * @param id l'identifiant unique de l'organisation
     * @return la liste des demandes d'équipements liées à l'organisation
     */
    public List<EquipmentRequestDTO> getEquipmentRequestOnOrganization(String id) {
        Optional<Organization> org = this.repo.findById(Long.parseLong(id));
        if (org.isEmpty()) {
            throw new UnknownID("L'organisation recherchée est inconnue");
        }
        return EquipmentMapper.toDto(org.get().getEquipmentRequests());
    }

    @Transactional
    public void createOrganization(UserDetailsImpl details, OrganizationDTO org) {
        save(org, details);
    }

    @Transactional
    public OrganizationDTO save(OrganizationDTO dto, UserDetailsImpl userDetails) {
        Organization organization;
        if (dto.getId() != null) {
            if (userDetails == null || !repo.isAdminOfOrganization(userDetails.getId(), dto.getId())) {
                throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
            }
            // Update existing organization
            organization = repo.findById(dto.getId())
                    .orElseThrow(() -> new UnknownID("Organisation inconnue"));

            organization.setName(dto.getName());
            organization.setCity(dto.getCity());
            organization.setFormationType(dto.getFormationType());
            organization.setDescription(dto.getDescription());
            organization.setConcertDress(dto.getConcertDress());
            organization.setLogisticsInfo(dto.getLogisticsInfo());
            organization.setMusicianCount(dto.getMusicianCount());
            organization.setRehearsalAddress(dto.getRehearsalAddress());
            organization.setRehearsalSchedule(dto.getRehearsalSchedule());

            updateConcerts(organization, dto.getConcerts());
            updateAnnualNeeds(organization, dto.getAnnualNeeds());
            updateRehearsalManuals(organization, dto.getRehearsalManuals());
            updateRehearsalSchedules(organization, dto.getRehearsalSchedules());
        } else {
            // Create new organization
            organization = OrganizationMapper.toEntity(dto);
            organization.setCreatedAt(Instant.now());
            if (userDetails != null) {
                User user = userRepository.findById(userDetails.getId())
                        .orElseThrow(() -> new UnknownID("Utilisateur administrateur non trouvé"));
                OrganizationAdmin admin = new OrganizationAdmin();
                OrganizationAdminId adminId = new OrganizationAdminId();
                adminId.setUserId(user.getId());
                admin.setId(adminId);
                admin.setUser(user);
                admin.setOrganization(organization);
                admin.setAddedAt(Instant.now());
                organization.getOrganizationAdmins().add(admin);
            }
        }

        Organization saved = repo.save(organization);

        // Envoyer la notification email lors de la création d'une nouvelle organisation
        if (dto.getId() == null && userDetails != null) {
            userRepository.findById(userDetails.getId()).ifPresent(user -> {
                if (user.getEmail() != null) {
                    emailService.sendOrganizationCreatedNotification(user.getEmail(), saved.getName());
                }
            });
        }

        return OrganizationMapper.toDto(saved);
    }

    /**
     * Ajoute un nouvel administrateur à une organisation existante.
     *
     * @param orgId       l'identifiant de l'organisation
     * @param username    le nom d'utilisateur ou email du futur administrateur
     * @param userDetails l'utilisateur actuellement connecté effectuant l'action
     * @return le DTO de l'organisation mis à jour
     */
    @Transactional
    public OrganizationDTO addAdmin(Long orgId, String username, UserDetailsImpl userDetails) {
        if (userDetails == null || !repo.isAdminOfOrganization(userDetails.getId(), orgId)) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
        }

        Organization org = repo.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        User targetUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UnknownID("Utilisateur introuvable avec le pseudo ou l'email : " + username));

        boolean alreadyAdmin = org.getOrganizationAdmins().stream()
                .anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(targetUser.getId()));
        if (alreadyAdmin) {
            throw new IllegalArgumentException("L'utilisateur est déjà administrateur de cette organisation");
        }

        OrganizationAdmin admin = new OrganizationAdmin();
        OrganizationAdminId adminId = new OrganizationAdminId();
        adminId.setUserId(targetUser.getId());
        adminId.setOrganizationId(org.getId());
        admin.setId(adminId);
        admin.setUser(targetUser);
        admin.setOrganization(org);
        admin.setAddedAt(Instant.now());

        org.getOrganizationAdmins().add(admin);
        Organization saved = repo.save(org);

        if (targetUser.getEmail() != null) {
            emailService.sendAdminPromotedNotification(targetUser.getEmail(), org.getName(), userDetails.getUsername());
        }

        return OrganizationMapper.toDto(saved);
    }

    /**
     * Supprime un administrateur d'une organisation existante.
     *
     * @param orgId       l'identifiant de l'organisation
     * @param username    le pseudo ou email de l'administrateur à retirer
     * @param userDetails l'utilisateur actuellement connecté effectuant l'action
     * @return le DTO de l'organisation mis à jour
     */
    @Transactional
    public OrganizationDTO removeAdmin(Long orgId, String username, UserDetailsImpl userDetails) {
        if (userDetails == null || !repo.isAdminOfOrganization(userDetails.getId(), orgId)) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
        }

        Organization org = repo.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        User targetUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UnknownID("Utilisateur introuvable avec le pseudo ou l'email : " + username));

        if (targetUser.getId().equals(userDetails.getId()) ||
            targetUser.getUsername().equalsIgnoreCase(userDetails.getUsername())) {
            throw new IllegalArgumentException("Vous ne pouvez pas vous retirer vous-même des administrateurs de l'organisation");
        }

        if (org.getOrganizationAdmins().size() <= 1) {
            throw new IllegalStateException("Impossible de supprimer le dernier administrateur de l'organisation");
        }

        boolean removed = org.getOrganizationAdmins().removeIf(a ->
                (a.getUser() != null && a.getUser().getId().equals(targetUser.getId())) ||
                (a.getId() != null && a.getId().getUserId() != null && a.getId().getUserId().equals(targetUser.getId()))
        );

        if (!removed) {
            throw new UnknownID("L'utilisateur n'est pas administrateur de cette organisation");
        }

        Organization saved = repo.save(org);
        return OrganizationMapper.toDto(saved);
    }

    private void updateConcerts(Organization org, List<ConcertDTO> dtos) {
        if (dtos == null) return;
        List<Concert> current = org.getConcerts();
        current.removeIf(c -> c.getId() != null && dtos.stream().noneMatch(d -> d.getId() != null && d.getId().equals(c.getId())));
        for (ConcertDTO dto : dtos) {
            if (dto.getId() != null) {
                current.stream()
                        .filter(c -> c.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(c -> {
                            c.setTitle(dto.getTitle());
                            c.setEventDate(dto.getEventDate());
                            c.setLocation(dto.getLocation());
                            c.setDescription(dto.getDescription());
                        });
            } else {
                Concert newConcert = OrganizationMapper.toEntity(dto);
                newConcert.setOrganization(org);
                current.add(newConcert);
            }
        }
    }

    private void updateAnnualNeeds(Organization org, List<AnnualNeedDTO> dtos) {
        if (dtos == null) return;
        List<AnnualNeeds> current = org.getAnnualNeeds();
        current.removeIf(n -> n.getId() != null && dtos.stream().noneMatch(d -> d.getId() != null && d.getId().equals(n.getId())));
        for (AnnualNeedDTO dto : dtos) {
            Integer count = dto.getRequiredCount() != null ? dto.getRequiredCount() : dto.getCount();
            if (dto.getId() != null) {
                current.stream()
                        .filter(n -> n.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(n -> {
                            n.setInstrument(dto.getInstrument());
                            n.setRequiredCount(count != null ? count : 0);
                            n.setDescription(dto.getDescription());
                        });
            } else {
                AnnualNeeds newNeed = OrganizationMapper.toEntity(dto);
                newNeed.setRequiredCount(count != null ? count : 0);
                newNeed.setOrganization(org);
                current.add(newNeed);
            }
        }
    }

    private void updateRehearsalManuals(Organization org, List<RehearsalManualDTO> dtos) {
        if (dtos == null) return;
        List<RehearsalManual> current = org.getRehearsalManuals();
        current.removeIf(m -> m.getId() != null && dtos.stream().noneMatch(d -> d.getId() != null && d.getId().equals(m.getId())));
        for (RehearsalManualDTO dto : dtos) {
            if (dto.getId() != null) {
                current.stream()
                        .filter(m -> m.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(m -> {
                            m.setManualDate(dto.getManualDate());
                            m.setStartTime(dto.getStartTime());
                            m.setEndTime(dto.getEndTime());
                            m.setLocation(dto.getLocation());
                        });
            } else {
                RehearsalManual newManual = OrganizationMapper.toEntity(dto);
                newManual.setOrganization(org);
                current.add(newManual);
            }
        }
    }

    private void updateRehearsalSchedules(Organization org, List<RehearsalScheduleDTO> dtos) {
        if (dtos == null) return;
        List<RehearsalSchedule> current = org.getRehearsalSchedules();
        current.removeIf(s -> s.getId() != null && dtos.stream().noneMatch(d -> d.getId() != null && d.getId().equals(s.getId())));
        for (RehearsalScheduleDTO dto : dtos) {
            if (dto.getId() != null) {
                current.stream()
                        .filter(s -> s.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(s -> {
                            s.setDayOfWeek(dto.getDayOfWeek());
                            s.setStartTime(dto.getStartTime());
                            s.setEndTime(dto.getEndTime());
                            s.setLocation(dto.getLocation());
                        });
            } else {
                RehearsalSchedule newSchedule = OrganizationMapper.toEntity(dto);
                newSchedule.setOrganization(org);
                current.add(newSchedule);
            }
        }
    }
}
