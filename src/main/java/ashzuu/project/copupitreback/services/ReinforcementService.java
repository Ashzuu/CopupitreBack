package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.ReinforcementRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.ReinforcementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service gérant la logique métier liée aux demandes et annonces de renfort.
 * <p>
 * Ce service permet de récupérer et gérer les annonces de renfort pour des événements ou orchestres.
 * </p>
 */
@Service
public class ReinforcementService {

    /**
     * Dépôt permettant de communiquer avec la table des renforts en base de données.
     */
    private final ReinforcementRepository repo;

    /**
     * Dépôt permettant de communiquer avec la table des organisations en base de données.
     */
    private final OrganizationRepository orgRepo;

    /**
     * Dépôt permettant de communiquer avec la table des utilisateurs en base de données.
     */
    private final UserRepository userRepo;

    /**
     * Constructeur pour injecter les dépendances requises.
     *
     * @param repo le dépôt des renforts
     * @param orgRepo le dépôt des organisations
     * @param userRepo le dépôt des utilisateurs
     */
    public ReinforcementService(ReinforcementRepository repo, OrganizationRepository orgRepo, UserRepository userRepo){
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
    }

    /**
     * Récupère l'ensemble des annonces de renfort sous forme de DTO.
     * <p>
     * Cette méthode interroge la base de données pour récupérer toutes les annonces de renfort existantes,
     * puis les convertit en DTO en utilisant {@link ReinforcementMapper}.
     * </p>
     *
     * @return la liste de toutes les annonces de renfort sous forme de {@link ReinforcementAnnouncementDTO}
     */
    public List<ReinforcementAnnouncementDTO> getAllAnnouncements(){
        return ReinforcementMapper.toAnnouncementDto(this.repo.findAll());
    }

    /**
     * Crée ou met à jour une annonce de renfort et met à jour ses candidatures.
     *
     * @param dto l'annonce de renfort sous forme de DTO
     * @return l'annonce de renfort enregistrée sous forme de DTO
     */
    @Transactional
    public ReinforcementAnnouncementDTO saveAnnouncement(ReinforcementAnnouncementDTO dto) {
        ReinforcementRequest request;
        Organization organization = null;
        if (dto.getOrganizationId() != null) {
            organization = orgRepo.findById(dto.getOrganizationId())
                    .orElseThrow(() -> new UnknownID("Organisation inconnue"));
        }

        if (dto.getId() != null) {
            request = repo.findById(dto.getId())
                    .orElseThrow(() -> new UnknownID("Demande de renfort inconnue"));
            request.setTitle(dto.getTitle());
            request.setInstrumentNeeded(dto.getInstrumentNeeded());
            request.setRequiredQuantity(dto.getRequiredQuantity());
            request.setEventDate(dto.getEventDate());
            request.setEventLocation(dto.getEventLocation());
            request.setDescription(dto.getDescription());
            request.setDeadline(dto.getDeadline());
            request.setStatus(dto.getStatus());
            request.setRepertoireDetails(dto.getRepertoireDetails());
            if (organization != null) {
                request.setOrganization(organization);
            }

            updateApplications(request, dto.getResponses());
        } else {
            request = ReinforcementMapper.toEntity(dto);
            if (organization != null) {
                request.setOrganization(organization);
            }
            request.setCreatedAt(Instant.now());
            if (request.getStatus() == null) {
                request.setStatus("OPEN");
            }

            if (dto.getResponses() != null) {
                List<Application> apps = new ArrayList<>();
                for (ReinforcementResponseDTO respDto : dto.getResponses()) {
                    Application app = ReinforcementMapper.toEntity(respDto);
                    app.setRequest(request);
                    if (respDto.getRespondentUsername() != null) {
                        User user = userRepo.findByUsername(respDto.getRespondentUsername())
                                .orElseThrow(() -> new UnknownID("Musicien inconnu: " + respDto.getRespondentUsername()));
                        app.setUser(user);
                    }
                    if (app.getAppliedAt() == null) {
                        app.setAppliedAt(Instant.now());
                    }
                    apps.add(app);
                }
                request.setApplications(apps);
            }
        }

        ReinforcementRequest saved = repo.save(request);
        return ReinforcementMapper.toAnnouncementDto(saved);
    }

    private void updateApplications(ReinforcementRequest request, List<ReinforcementResponseDTO> dtos) {
        if (dtos == null) return;
        List<Application> current = request.getApplications();

        // Supprimer les candidatures qui ne sont pas dans dtos
        current.removeIf(a -> a.getId() != null && dtos.stream().noneMatch(d -> d.getId() != null && d.getId().equals(a.getId())));

        // Mettre à jour les existantes et ajouter les nouvelles
        for (ReinforcementResponseDTO dto : dtos) {
            if (dto.getId() != null) {
                current.stream()
                        .filter(a -> a.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(a -> {
                            a.setStatus(dto.getStatus());
                            a.setAppliedAt(dto.getRespondedAt());
                            if (dto.getRespondentUsername() != null) {
                                userRepo.findByUsername(dto.getRespondentUsername())
                                        .ifPresent(a::setUser);
                            }
                        });
            } else {
                Application newApp = ReinforcementMapper.toEntity(dto);
                newApp.setRequest(request);
                if (dto.getRespondentUsername() != null) {
                    userRepo.findByUsername(dto.getRespondentUsername())
                            .ifPresent(newApp::setUser);
                }
                if (newApp.getAppliedAt() == null) {
                    newApp.setAppliedAt(Instant.now());
                }
                current.add(newApp);
            }
        }
    }
}
