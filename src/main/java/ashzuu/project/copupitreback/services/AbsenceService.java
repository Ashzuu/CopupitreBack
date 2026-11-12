package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.absence.*;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.*;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.AbsenceRepository;
import ashzuu.project.copupitreback.jpa.repositories.MusicianRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.mapper.AbsenceMapper;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final OrganizationRepository organizationRepository;
    private final MusicianRepository musicianRepository;
    private final NotificationService notificationService;
    private Clock clock = Clock.systemDefaultZone();

    public AbsenceService(
            AbsenceRepository absenceRepository,
            OrganizationRepository organizationRepository,
            MusicianRepository musicianRepository,
            NotificationService notificationService
    ) {
        this.absenceRepository = absenceRepository;
        this.organizationRepository = organizationRepository;
        this.musicianRepository = musicianRepository;
        this.notificationService = notificationService;
    }

    /**
     * Permet de modifier l'horloge système utilisée pour tester les délais RG-ABS-01.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Enregistre une absence pour une répétition ou un concert (RG-ABS-01, RG-ABS-04, US-ABS-01).
     */
    @Transactional
    public AbsenceDTO registerAbsence(Long orgId, CreateAbsenceRequest req, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        Musician musician = musicianRepository.findById(req.getMusicianId())
                .orElseThrow(() -> new UnknownID("Musicien non trouvé avec l'identifiant : " + req.getMusicianId()));

        if (musician.getOrganizations().stream().noneMatch(o -> o.getId().equals(orgId))) {
            throw new IllegalArgumentException("Ce musicien ne fait pas partie de cette organisation.");
        }

        boolean isAdmin = organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId);
        boolean isSelf = musician.getUser() != null && musician.getUser().getId().equals(userDetails.getId());

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à déclarer une absence pour ce musicien.");
        }

        AbsenceStatus status;
        boolean submittedByAdmin = isAdmin;

        if (isAdmin) {
            // Saisie par l'administrateur (RG-ABS-04) : statut justifiée ou non justifiée
            status = req.getStatus() != null ? req.getStatus() : AbsenceStatus.JUSTIFIED;
        } else {
            // Saisie par le musicien (RG-ABS-01) : contrôle des délais
            LocalDateTime now = LocalDateTime.now(clock);
            LocalDateTime eventDateTime = req.getEventDate().atStartOfDay();

            boolean late;
            if (req.getEventType() == EventType.REHEARSAL) {
                // Minimum 24h avant l'événement
                late = now.isAfter(eventDateTime.minusHours(24));
            } else {
                // Minimum 1 semaine (7 jours) avant le concert
                late = now.isAfter(eventDateTime.minusDays(7));
            }

            if (late) {
                status = AbsenceStatus.UNJUSTIFIED;
                // Avertir les administrateurs de l'organisation
                notifyAdminsOfLateAbsence(org, musician, req);
            } else {
                status = AbsenceStatus.PENDING;
            }
        }

        Absence absence = Absence.builder()
                .organization(org)
                .musician(musician)
                .eventType(req.getEventType())
                .eventDate(req.getEventDate())
                .endDate(req.getEndDate())
                .reason(req.getReason())
                .status(status)
                .submittedByAdmin(submittedByAdmin)
                .build();

        Absence saved = absenceRepository.save(absence);
        return AbsenceMapper.toDto(saved);
    }

    /**
     * Récupère la liste des absences filtrées pour une organisation (RG-ABS-02, RG-ABS-05, US-ABS-02, US-ABS-03).
     */
    public List<AbsenceDTO> getAbsences(
            Long orgId,
            LocalDate startDate,
            LocalDate endDate,
            EventType eventType,
            AbsenceStatus status,
            Long musicianId,
            UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return Collections.emptyList();
        }
        boolean isAdmin = organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId);
        boolean isMember = musicianRepository.findByOrganizationIdAndUserId(orgId, userDetails.getId()).isPresent();
        if (!isAdmin && !isMember) {
            return Collections.emptyList();
        }
        return AbsenceMapper.toDtoList(absenceRepository.findByFilters(orgId, startDate, endDate, eventType, status, musicianId));
    }

    /**
     * Calcule les compteurs d'absences par musicien sur une période donnée (RG-ABS-02, US-ABS-02).
     */
    public List<AbsenceStatsDTO> getMusicianAbsenceStats(
            Long orgId,
            LocalDate startDate,
            LocalDate endDate,
            UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return Collections.emptyList();
        }
        boolean isAdmin = organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId);
        boolean isMember = musicianRepository.findByOrganizationIdAndUserId(orgId, userDetails.getId()).isPresent();
        if (!isAdmin && !isMember) {
            return Collections.emptyList();
        }

        List<Musician> musicians = musicianRepository.findAllByOrganizationId(orgId);
        List<Absence> absences = absenceRepository.findByOrganizationIdAndDateRange(orgId, startDate, endDate);

        List<AbsenceStatsDTO> statsList = new ArrayList<>();
        for (Musician m : musicians) {
            List<Absence> musicianAbsences = absences.stream()
                    .filter(a -> a.getMusician().getId().equals(m.getId()))
                    .toList();

            long total = musicianAbsences.size();
            long justified = musicianAbsences.stream().filter(a -> a.getStatus() == AbsenceStatus.JUSTIFIED).count();
            long unjustified = musicianAbsences.stream().filter(a -> a.getStatus() == AbsenceStatus.UNJUSTIFIED).count();
            long pending = musicianAbsences.stream().filter(a -> a.getStatus() == AbsenceStatus.PENDING).count();

            statsList.add(AbsenceStatsDTO.builder()
                    .musicianId(m.getId())
                    .musicianName(m.getFirstName() + " " + m.getLastName())
                    .musicianEmail(m.getEmail())
                    .instrument(m.getInstrument())
                    .totalAbsences(total)
                    .justifiedCount(justified)
                    .unjustifiedCount(unjustified)
                    .pendingCount(pending)
                    .build());
        }

        return statsList;
    }

    /**
     * Met à jour le statut d'une absence (RG-ABS-04).
     */
    @Transactional
    public AbsenceDTO updateAbsenceStatus(
            Long orgId,
            Long absenceId,
            UpdateAbsenceStatusRequest req,
            UserDetailsImpl userDetails
    ) {
        checkAdminAccess(orgId, userDetails);

        Absence absence = absenceRepository.findById(absenceId)
                .orElseThrow(() -> new UnknownID("Absence non trouvée avec l'identifiant : " + absenceId));

        if (!absence.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Cette absence n'appartient pas à cette organisation.");
        }

        absence.setStatus(req.getStatus());
        Absence saved = absenceRepository.save(absence);
        return AbsenceMapper.toDto(saved);
    }

    /**
     * Avertit les membres de l'organisation lors de l'annulation d'une répétition ou d'un concert (RG-ABS-03, UC-ABS-04).
     */
    @Transactional
    public void notifyEventCancellation(
            Long orgId,
            CancelEventNotificationRequest req,
            UserDetailsImpl userDetails
    ) {
        checkAdminAccess(orgId, userDetails);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new UnknownID("Organisation non trouvée avec l'identifiant : " + orgId));

        List<Musician> musicians = musicianRepository.findAllByOrganizationId(orgId);
        String eventLabel = req.getEventType() == EventType.REHEARSAL ? "Répétition" : "Concert";
        String title = "Annulation de " + eventLabel + " (" + org.getName() + ")";
        String content = "L'événement prévu le " + req.getEventDate() + " a été annulé. Motif : " + req.getMessage();
        String link = "/organization/" + org.getId();

        Set<Long> notifiedUserIds = new HashSet<>();
        for (Musician m : musicians) {
            if (m.getUser() != null && notifiedUserIds.add(m.getUser().getId())) {
                notificationService.createNotification(
                        m.getUser(),
                        "EVENT_CANCELLED",
                        title,
                        content,
                        link,
                        "ORGANIZATION",
                        org.getId()
                );
            }
        }
    }

    private void notifyAdminsOfLateAbsence(Organization org, Musician musician, CreateAbsenceRequest req) {
        List<User> admins = organizationRepository.findAdminsByOrganizationId(org.getId());
        String eventLabel = req.getEventType() == EventType.REHEARSAL ? "répétition" : "concert";
        String title = "Absence tardive déclarée (" + org.getName() + ")";
        String content = "Le musicien " + musician.getFirstName() + " " + musician.getLastName() +
                " a déclaré une absence hors délai pour le/la " + eventLabel + " du " + req.getEventDate() +
                ". Statut : Non justifiée. Motif : " + req.getReason();
        String link = "/organization/" + org.getId();

        for (User admin : admins) {
            notificationService.createNotification(
                    admin,
                    "ABSENCE_LATE_ALERT",
                    title,
                    content,
                    link,
                    "ORGANIZATION",
                    org.getId()
            );
        }
    }

    private void checkAdminAccess(Long orgId, UserDetailsImpl userDetails) {
        if (userDetails == null || !organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId)) {
            throw new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation");
        }
    }

    private void checkMemberOrAdminAccess(Long orgId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        boolean isAdmin = organizationRepository.isAdminOfOrganization(userDetails.getId(), orgId);
        boolean isMember = musicianRepository.findByOrganizationIdAndUserId(orgId, userDetails.getId()).isPresent();
        if (!isAdmin && !isMember) {
            throw new AccessDeniedException("Accès réservé aux membres et administrateurs de l'organisation");
        }
    }
}
