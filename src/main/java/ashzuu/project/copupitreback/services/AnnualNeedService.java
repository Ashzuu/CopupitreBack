package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeedApplication;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeeds;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.AnnualNeedApplicationRepository;
import ashzuu.project.copupitreback.jpa.repositories.AnnualNeedRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.mapper.PermanentMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service gérant la logique métier liée aux recrutements permanents (besoins annuels) et à leurs candidatures.
 */
@Service
public class AnnualNeedService {

    private final AnnualNeedRepository repo;
    private final AnnualNeedApplicationRepository applicationRepo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public AnnualNeedService(
            AnnualNeedRepository repo,
            AnnualNeedApplicationRepository applicationRepo,
            OrganizationRepository orgRepo,
            UserRepository userRepo,
            EmailService emailService) {
        this.repo = repo;
        this.applicationRepo = applicationRepo;
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    /**
     * Récupère l'ensemble des annonces de recrutement permanent (besoins annuels de tous les orchestres).
     */
    public List<PermanentAnnouncementDTO> getAllPermanentAnnouncements() {
        return PermanentMapper.toDtoList(this.repo.findAllWithOrganization());
    }

    /**
     * Récupère les besoins annuels d'une organisation spécifique.
     */
    public List<PermanentAnnouncementDTO> getPermanentAnnouncementsByOrganization(Long organizationId) {
        return PermanentMapper.toDtoList(this.repo.findByOrganizationId(organizationId));
    }

    /**
     * Récupère les besoins annuels des organisations administrées par l'utilisateur connecté.
     */
    public List<PermanentAnnouncementDTO> getMyPermanentAnnouncements(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return PermanentMapper.toDtoList(this.repo.findByAdminUserId(userDetails.getId()));
    }

    /**
     * Récupère toutes les candidatures faites par l'utilisateur connecté aux postes permanents.
     */
    public List<ReinforcementResponseDTO> getMyApplications(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return PermanentMapper.toResponseDtoList(this.applicationRepo.findByUserId(userDetails.getId()));
    }

    /**
     * Permet à un musicien connecté de postuler à un besoin annuel de recrutement (poste permanent).
     */
    @Transactional
    public ReinforcementResponseDTO applyToAnnualNeed(Long annualNeedId, UserDetailsImpl userDetails, String message) {
        if (userDetails == null) {
            throw new AccessDeniedException("Vous devez être connecté pour postuler");
        }

        AnnualNeeds need = repo.findById(annualNeedId)
                .orElseThrow(() -> new UnknownID("Besoin annuel introuvable : " + annualNeedId));

        // Règle : Les administrateurs d'une organisation ne peuvent pas postuler à leurs propres besoins
        if (need.getOrganization() != null && orgRepo.isAdminOfOrganization(userDetails.getId(), need.getOrganization().getId())) {
            throw new IllegalArgumentException("Les administrateurs d'une organisation ne peuvent pas postuler aux besoins de leur propre organisation");
        }

        // Règle : Unicité de la candidature
        if (applicationRepo.existsByAnnualNeedIdAndUserId(annualNeedId, userDetails.getId())) {
            throw new IllegalArgumentException("Vous avez déjà postulé à cette annonce");
        }

        User user = userRepo.findById(userDetails.getId())
                .orElseThrow(() -> new UnknownID("Utilisateur introuvable : " + userDetails.getId()));

        AnnualNeedApplication application = AnnualNeedApplication.builder()
                .annualNeed(need)
                .user(user)
                .status("PENDING")
                .appliedAt(Instant.now())
                .message(message)
                .build();

        AnnualNeedApplication saved = applicationRepo.save(application);
        return PermanentMapper.toResponseDto(saved);
    }

    /**
     * Permet à l'administrateur d'accepter ou de refuser une candidature à un poste permanent.
     */
    @Transactional
    public ReinforcementResponseDTO updateApplicationStatus(
            Long annualNeedId,
            Long applicationId,
            String newStatus,
            UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        AnnualNeeds need = repo.findById(annualNeedId)
                .orElseThrow(() -> new UnknownID("Besoin annuel introuvable : " + annualNeedId));

        // Vérification des droits administrateur sur l'organisation
        if (need.getOrganization() == null || !orgRepo.isAdminOfOrganization(userDetails.getId(), need.getOrganization().getId())) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de l'organisation émettrice");
        }

        AnnualNeedApplication application = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new UnknownID("Candidature introuvable : " + applicationId));

        if (application.getAnnualNeed() == null || !application.getAnnualNeed().getId().equals(annualNeedId)) {
            throw new IllegalArgumentException("Cette candidature ne correspond pas à l'annonce spécifiée");
        }

        String normalizedStatus = newStatus != null ? newStatus.trim().toUpperCase() : "";
        if (!"ACCEPTED".equals(normalizedStatus) && !"REJECTED".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Le statut doit être ACCEPTED ou REJECTED");
        }

        application.setStatus(normalizedStatus);
        AnnualNeedApplication saved = applicationRepo.save(application);

        // Envoi de l'alerte par courriel
        if (application.getUser() != null && application.getUser().getEmail() != null) {
            String orgName = need.getOrganization() != null ? need.getOrganization().getName() : "Organisation";
            emailService.sendApplicationStatusUpdate(
                    application.getUser().getEmail(),
                    application.getUser().getUsername(),
                    normalizedStatus,
                    orgName
            );
        }

        return PermanentMapper.toResponseDto(saved);
    }
}
