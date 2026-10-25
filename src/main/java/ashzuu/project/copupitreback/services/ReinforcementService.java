package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.ApplicationRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.ReinforcementRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
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
 * Service gérant la logique métier liée aux demandes et annonces de renfort.
 */
@Service
public class ReinforcementService {

    private final ReinforcementRepository repo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final ApplicationRepository applicationRepo;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    public ReinforcementService(
            ReinforcementRepository repo,
            OrganizationRepository orgRepo,
            UserRepository userRepo,
            ApplicationRepository applicationRepo,
            EmailService emailService,
            NotificationService notificationService,
            SubscriptionService subscriptionService){
        this.repo = repo;
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.applicationRepo = applicationRepo;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Récupère l'ensemble des annonces de renfort sous forme de DTO.
     */
    public List<ReinforcementAnnouncementDTO> getAllAnnouncements(){
        return ReinforcementMapper.toAnnouncementDto(this.repo.findAll());
    }

    /**
     * Récupère les annonces de renfort des organisations administrées par l'utilisateur connecté.
     */
    public List<ReinforcementAnnouncementDTO> getMyReinforcements(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return ReinforcementMapper.toAnnouncementDto(this.repo.findByAdminUserId(userDetails.getId()));
    }

    /**
     * Récupère l'ensemble des réponses/candidatures pour une annonce donnée (US-DASH-04).
     * Accessible uniquement aux administrateurs de l'organisation propriétaire de l'annonce.
     */
    public List<ReinforcementResponseDTO> getAllResponses(Long id, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        ReinforcementRequest request = repo.findById(id)
                .orElseThrow(() -> new UnknownID("Demande de renfort inconnue : " + id));

        if (!orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation.");
        }

        return ReinforcementMapper.toResponseDto(this.applicationRepo.findAllByIdRequest(id));
    }

    public List<ReinforcementResponseDTO> getAllResponses(String id, UserDetailsImpl userDetails) {
        return getAllResponses(Long.parseLong(id), userDetails);
    }

    /**
     * Récupère toutes les candidatures effectuées par l'utilisateur connecté (US-RENF-03).
     */
    public List<ReinforcementResponseDTO> getMyApplications(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return ReinforcementMapper.toResponseDto(this.applicationRepo.findByUserId(userDetails.getId()));
    }

    /**
     * Permet à un musicien connecté de postuler à une annonce de renfort ouverte (US-RENF-03).
     */
    @Transactional
    public ReinforcementResponseDTO applyToAnnouncement(Long announcementId, UserDetailsImpl userDetails, String message) {
        if (userDetails == null) {
            throw new AccessDeniedException("Vous devez être connecté pour postuler");
        }

        ReinforcementRequest request = repo.findById(announcementId)
                .orElseThrow(() -> new UnknownID("Demande de renfort inconnue : " + announcementId));

        if (!"OPEN".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Cette annonce n'accepte plus de candidatures (statut: " + request.getStatus() + ")");
        }

        // Règle : Les administrateurs d'une organisation ne peuvent pas postuler à leurs propres annonces
        if (request.getOrganization() != null && orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
            throw new IllegalArgumentException("Les administrateurs d'une organisation ne peuvent pas postuler à leurs propres annonces de renfort");
        }

        // Règle RG-RENF-05 : Unicité de candidature
        if (applicationRepo.existsByRequestIdAndUserId(announcementId, userDetails.getId())) {
            throw new IllegalArgumentException("Vous avez déjà postulé à cette annonce");
        }

        User user = userRepo.findById(userDetails.getId())
                .orElseThrow(() -> new UnknownID("Utilisateur introuvable : " + userDetails.getId()));

        Application application = Application.builder()
                .request(request)
                .user(user)
                .status("PENDING")
                .appliedAt(Instant.now())
                .message(message)
                .build();

        Application saved = applicationRepo.save(application);

        // Notification in-app des administrateurs de l'organisation (US-COM-02)
        if (request.getOrganization() != null) {
            List<User> admins = orgRepo.findAdminsByOrganizationId(request.getOrganization().getId());
            for (User admin : admins) {
                notificationService.createNotification(
                        admin,
                        "NEW_APPLICATION",
                        "Nouvelle candidature reçue",
                        user.getFirstName() + " " + user.getLastName() + " a postulé pour : " + request.getTitle(),
                        "/dashboard?contextType=APPLICATION&contextId=" + saved.getId(),
                        "APPLICATION",
                        saved.getId()
                );
            }
        }

        return ReinforcementMapper.toResponseDto(saved);
    }

    /**
     * Permet à l'administrateur d'accepter ou refuser une candidature (US-RENF-04).
     */
    @Transactional
    public ReinforcementResponseDTO updateApplicationStatus(
            Long announcementId,
            Long applicationId,
            String newStatus,
            UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        ReinforcementRequest request = repo.findById(announcementId)
                .orElseThrow(() -> new UnknownID("Demande de renfort inconnue : " + announcementId));

        // Vérification des droits administrateur sur l'organisation
        if (request.getOrganization() == null || !orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de l'organisation émettrice");
        }

        Application application = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new UnknownID("Candidature introuvable : " + applicationId));

        if (application.getRequest() == null || !application.getRequest().getId().equals(announcementId)) {
            throw new IllegalArgumentException("Cette candidature ne correspond pas à l'annonce spécifiée");
        }

        String normalizedStatus = newStatus != null ? newStatus.trim().toUpperCase() : "";
        if (!"ACCEPTED".equals(normalizedStatus) && !"REJECTED".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Le statut doit être ACCEPTED ou REJECTED");
        }

        application.setStatus(normalizedStatus);
        Application saved = applicationRepo.save(application);

        // Si la candidature est acceptée ou refusée, recalcul de l'objectif (RG-RENF-03 / RG-RENF-04)
        long acceptedCount = applicationRepo.countByRequestIdAndStatus(announcementId, "ACCEPTED");
        if (request.getRequiredQuantity() != null) {
            if (acceptedCount >= request.getRequiredQuantity()) {
                request.setStatus("OBJECTIVE_MET");
                repo.save(request);
            } else if ("OBJECTIVE_MET".equalsIgnoreCase(request.getStatus())) {
                request.setStatus("OPEN");
                repo.save(request);
            }
        }

        // Envoi de la notification par courriel (via l'interface EmailService)
        String orgName = request.getOrganization() != null ? request.getOrganization().getName() : "Organisation";
        if (application.getUser() != null) {
            String statusLabel = "ACCEPTED".equals(normalizedStatus) ? "acceptée" : "refusée";
            notificationService.createNotification(
                    application.getUser(),
                    "APPLICATION_STATUS",
                    "Candidature " + statusLabel,
                    "Votre candidature pour \"" + request.getTitle() + "\" auprès de " + orgName + " a été " + statusLabel + ".",
                    "/mes-candidatures?contextType=APPLICATION&contextId=" + application.getId(),
                    "APPLICATION",
                    application.getId()
            );

            if (application.getUser().getEmail() != null) {
                emailService.sendApplicationStatusUpdate(
                        application.getUser().getEmail(),
                        application.getUser().getUsername(),
                        normalizedStatus,
                        orgName
                );
            }
        }

        return ReinforcementMapper.toResponseDto(saved);
    }

    /**
     * Crée ou met à jour une annonce de renfort et met à jour ses candidatures.
     */
    @Transactional
    public ReinforcementAnnouncementDTO saveAnnouncement(ReinforcementAnnouncementDTO dto, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        ReinforcementRequest request;
        Organization organization = null;
        boolean isNew = (dto.getId() == null);

        if (!isNew) {
            request = repo.findById(dto.getId())
                    .orElseThrow(() -> new UnknownID("Demande de renfort inconnue"));

            // Verify admin rights on current organization
            if (request.getOrganization() != null) {
                if (!orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
                    throw new AccessDeniedException("Vous n'êtes pas administrateur de l'organisation");
                }
            }

            // If changing organization, verify admin rights on new one
            if (dto.getOrganizationId() != null && (request.getOrganization() == null || !dto.getOrganizationId().equals(request.getOrganization().getId()))) {
                if (!orgRepo.isAdminOfOrganization(userDetails.getId(), dto.getOrganizationId())) {
                    throw new AccessDeniedException("Vous n'êtes pas administrateur de la nouvelle organisation");
                }
            }

            if (dto.getOrganizationId() != null) {
                organization = orgRepo.findById(dto.getOrganizationId())
                        .orElseThrow(() -> new UnknownID("Organisation inconnue"));
            }

            request.setTitle(dto.getTitle());
            request.setInstrumentNeeded(dto.getInstrumentNeeded());
            request.setRequiredQuantity(dto.getRequiredQuantity() != null ? dto.getRequiredQuantity() : dto.getRequiredCount());
            request.setEventDate(dto.getEventDate());
            request.setEventLocation(dto.getEventLocation() != null ? dto.getEventLocation() : dto.getLocation());
            request.setDescription(dto.getDescription());
            request.setDeadline(dto.getDeadline());
            request.setStatus(dto.getStatus());
            request.setRepertoireDetails(dto.getRepertoireDetails());
            if (organization != null) {
                request.setOrganization(organization);
            }

            updateApplications(request, dto.getResponses());
        } else {
            // New request - check admin rights on organizationId
            if (dto.getOrganizationId() == null) {
                throw new IllegalArgumentException("L'identifiant de l'organisation est obligatoire pour créer une demande");
            }
            if (!orgRepo.isAdminOfOrganization(userDetails.getId(), dto.getOrganizationId())) {
                throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
            }

            if (dto.getOrganizationId() != null) {
                organization = orgRepo.findById(dto.getOrganizationId())
                        .orElseThrow(() -> new UnknownID("Organisation inconnue"));
            }

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

        // Alerte Email lors de la création d'une nouvelle annonce (RG-RENF-09)
        if (isNew && organization != null) {
            String orgEmail = organization.getName().toLowerCase().replaceAll("\\s+", "") + "@copupitre.fr";
            emailService.sendNewAnnouncementAlert(orgEmail, organization.getName(), saved.getTitle());

            // Notification in-app des musiciens abonnés à l'orchestre (US-COM-01 & US-COM-02)
            List<User> subscribers = subscriptionService.getSubscribedUsers(organization.getId());
            for (User subscriber : subscribers) {
                if (!subscriber.getId().equals(userDetails.getId())) {
                    notificationService.createNotification(
                            subscriber,
                            "NEW_ANNOUNCEMENT",
                            "Nouvelle annonce : " + saved.getTitle(),
                            "L'ensemble \"" + organization.getName() + "\" recherche : " + saved.getInstrumentNeeded() + ".",
                            "/renforts"
                    );
                }
            }
        }

        return ReinforcementMapper.toAnnouncementDto(saved);
    }

    private void updateApplications(ReinforcementRequest request, List<ReinforcementResponseDTO> dtos) {
        if (dtos == null) return;
        List<Application> current = request.getApplications();

        current.removeIf(a -> a.getId() != null && dtos.stream().noneMatch(d -> d.getId() != null && d.getId().equals(a.getId())));

        for (ReinforcementResponseDTO dto : dtos) {
            if (dto.getId() != null) {
                Optional<Application> existing = current.stream()
                        .filter(a -> a.getId() != null && a.getId().equals(dto.getId()))
                        .findFirst();
                if (existing.isPresent()) {
                    Application a = existing.get();
                    a.setStatus(dto.getStatus());
                    a.setAppliedAt(dto.getRespondedAt());
                    if (dto.getRespondentUsername() != null) {
                        userRepo.findByUsername(dto.getRespondentUsername())
                                .ifPresent(a::setUser);
                    }
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

    /**
     * Supprime une annonce de renfort si l'utilisateur est administrateur de l'organisation associée.
     */
    @Transactional
    public void deleteAnnouncement(Long id, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        ReinforcementRequest request = repo.findById(id)
                .orElseThrow(() -> new UnknownID("Demande de renfort inconnue"));

        if (request.getOrganization() != null) {
            if (!orgRepo.isAdminOfOrganization(userDetails.getId(), request.getOrganization().getId())) {
                throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
            }
        }

        repo.delete(request);
    }
}
