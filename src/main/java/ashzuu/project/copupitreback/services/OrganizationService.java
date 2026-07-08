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
 * ainsi que les annonces et demandes d'équipements qui y sont associées.
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
     * Constructeur pour injecter les dépendances requises.
     *
     * @param repo le dépôt des organisations
     * @param userRepository le dépôt des utilisateurs
     */
    public OrganizationService(OrganizationRepository repo, UserRepository userRepository){
        this.repo = repo;
        this.userRepository = userRepository;
    }

    /**
     * Récupère toutes les organisations de l'application sous forme de DTO.
     *
     * @return la liste de toutes les organisations sous forme de {@link OrganizationDTO}
     */
    public List<OrganizationDTO> getAll(){
        return OrganizationMapper.toDto(this.repo.findAll());
    }

    public OrganizationDTO getById(String id){
        Optional<Organization> org = this.repo.findById(Long.parseLong(id));
        if(org.isEmpty()){
            throw new UnknownID("L'organisation recherchée est inconnue");
        }
        return OrganizationMapper.toDto(org.get());
    }

    /**
     * Récupère les organisations associées à un utilisateur spécifique.
     * <p>
     * Cette méthode identifie l'utilisateur à partir de ses détails de connexion (session)
     * et récupère la liste des organisations dans lesquelles il est impliqué.
     * </p>
     *
     * @param details les détails de l'utilisateur connecté contenant son identifiant
     * @return la liste des organisations associées à cet utilisateur sous forme de {@link OrganizationDTO}
     */
    public List<OrganizationDTO> getOrganizationFromUser(UserDetailsImpl details){
        return OrganizationMapper.toDto(this.repo.getAllByUserId(details.getId()));
    }

    /**
     * Récupère la liste des annonces de renfort publiées pour une organisation spécifique.
     *
     * @param id l'identifiant unique de l'organisation
     * @return la liste des annonces de renfort liées à l'organisation
     */
    public List<ReinforcementAnnouncementDTO> getAnnouncementsOnOrganization(String id){
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
    public List<EquipmentRequestDTO> getEquipmentRequestOnOrganization(String id){
        Optional<Organization> org = this.repo.findById(Long.parseLong(id));
        if (org.isEmpty()) {
            throw new UnknownID("L'organisation recherchée est inconnue");
        }
        return EquipmentMapper.toDto(org.get().getEquipmentRequests());
    }

    @Transactional
    public void createOrganization(UserDetailsImpl details, OrganizationDTO org){
        save(org, details);
    }

    @Transactional
    public OrganizationDTO save(OrganizationDTO dto, UserDetailsImpl userDetails) {
        Organization organization;
        if (dto.getId() != null) {
            if (userDetails == null || !repo.isAdminOfOrganization(userDetails.getId(), dto.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
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
            if (dto.getId() != null) {
                current.stream()
                        .filter(n -> n.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(n -> {
                            n.setInstrument(dto.getInstrument());
                            n.setRequiredCount(dto.getRequiredCount());
                            n.setDescription(dto.getDescription());
                        });
            } else {
                AnnualNeeds newNeed = OrganizationMapper.toEntity(dto);
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
