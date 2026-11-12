package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.absence.*;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.*;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.AbsenceRepository;
import ashzuu.project.copupitreback.jpa.repositories.MusicianRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbsenceServiceTest {

    @Mock
    private AbsenceRepository absenceRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MusicianRepository musicianRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AbsenceService absenceService;

    private UserDetailsImpl adminUserDetails;
    private UserDetailsImpl musicianUserDetails;
    private Organization org;
    private Musician musician;
    private User musicianUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).username("adminUser").email("admin@copupitre.fr").role(ashzuu.project.copupitreback.jpa.entities.user.Role.USER).build();
        musicianUser = User.builder().id(2L).username("musicianUser").email("musician@copupitre.fr").role(ashzuu.project.copupitreback.jpa.entities.user.Role.USER).build();

        adminUserDetails = new UserDetailsImpl(adminUser);
        musicianUserDetails = new UserDetailsImpl(musicianUser);

        org = Organization.builder().id(10L).name("Orchestre Philharmonique").build();
        musician = Musician.builder()
                .id(100L)
                .firstName("Claire")
                .lastName("Fontaine")
                .email("musician@copupitre.fr")
                .instrument("Flûte")
                .user(musicianUser)
                .organizations(new HashSet<>(Collections.singletonList(org)))
                .build();

        // Fixer l'horloge au 2026-11-12 à 10h00
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2026, 11, 12, 10, 0).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        absenceService.setClock(fixedClock);
    }

    @Test
    void registerAbsence_byAdmin_setsJustifiedAndSubmittedByAdmin() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(musicianRepository.findById(100L)).thenReturn(Optional.of(musician));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> {
            Absence a = inv.getArgument(0);
            a.setId(500L);
            return a;
        });

        CreateAbsenceRequest req = CreateAbsenceRequest.builder()
                .musicianId(100L)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.of(2026, 11, 12)) // même jour
                .reason("Raison médicale")
                .status(AbsenceStatus.JUSTIFIED)
                .build();

        AbsenceDTO result = absenceService.registerAbsence(10L, req, adminUserDetails);
        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals(AbsenceStatus.JUSTIFIED, result.getStatus());
        assertTrue(result.isSubmittedByAdmin());
    }

    @Test
    void registerAbsence_byMusician_rehearsal_onTime_setsPending() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(musicianRepository.findById(100L)).thenReturn(Optional.of(musician));
        when(organizationRepository.isAdminOfOrganization(2L, 10L)).thenReturn(false);
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));

        // Now is 2026-11-12 10:00. Event on 2026-11-15 (> 24h avant).
        CreateAbsenceRequest req = CreateAbsenceRequest.builder()
                .musicianId(100L)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.of(2026, 11, 15))
                .reason("Déplacement professionnel")
                .build();

        AbsenceDTO result = absenceService.registerAbsence(10L, req, musicianUserDetails);
        assertEquals(AbsenceStatus.PENDING, result.getStatus());
        assertFalse(result.isSubmittedByAdmin());
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any());
    }

    @Test
    void registerAbsence_byMusician_rehearsal_late_setsUnjustifiedAndNotifiesAdmins() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(musicianRepository.findById(100L)).thenReturn(Optional.of(musician));
        when(organizationRepository.isAdminOfOrganization(2L, 10L)).thenReturn(false);
        when(organizationRepository.findAdminsByOrganizationId(10L)).thenReturn(List.of(adminUser));
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));

        // Now is 2026-11-12 10:00. Event is 2026-11-12 (moins de 24h!).
        CreateAbsenceRequest req = CreateAbsenceRequest.builder()
                .musicianId(100L)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.of(2026, 11, 12))
                .reason("Panne de réveil")
                .build();

        AbsenceDTO result = absenceService.registerAbsence(10L, req, musicianUserDetails);
        assertEquals(AbsenceStatus.UNJUSTIFIED, result.getStatus());
        assertFalse(result.isSubmittedByAdmin());

        // Admin averti
        verify(notificationService).createNotification(
                eq(adminUser), eq("ABSENCE_LATE_ALERT"), anyString(), anyString(), anyString(), eq("ORGANIZATION"), eq(10L)
        );
    }

    @Test
    void registerAbsence_byMusician_concert_late_setsUnjustified() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(musicianRepository.findById(100L)).thenReturn(Optional.of(musician));
        when(organizationRepository.isAdminOfOrganization(2L, 10L)).thenReturn(false);
        when(organizationRepository.findAdminsByOrganizationId(10L)).thenReturn(List.of(adminUser));
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));

        // Now is 2026-11-12. Concert is 2026-11-15 (3 jours avant, moins d'une semaine!).
        CreateAbsenceRequest req = CreateAbsenceRequest.builder()
                .musicianId(100L)
                .eventType(EventType.CONCERT)
                .eventDate(LocalDate.of(2026, 11, 15))
                .reason("Empêchement imprévu")
                .build();

        AbsenceDTO result = absenceService.registerAbsence(10L, req, musicianUserDetails);
        assertEquals(AbsenceStatus.UNJUSTIFIED, result.getStatus());
        verify(notificationService).createNotification(
                eq(adminUser), eq("ABSENCE_LATE_ALERT"), anyString(), anyString(), anyString(), eq("ORGANIZATION"), eq(10L)
        );
    }

    @Test
    void getMusicianAbsenceStats_computesCorrectCounts() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(musicianRepository.findAllByOrganizationId(10L)).thenReturn(List.of(musician));

        Absence a1 = Absence.builder().organization(org).musician(musician).status(AbsenceStatus.JUSTIFIED).build();
        Absence a2 = Absence.builder().organization(org).musician(musician).status(AbsenceStatus.UNJUSTIFIED).build();
        Absence a3 = Absence.builder().organization(org).musician(musician).status(AbsenceStatus.PENDING).build();

        when(absenceRepository.findByOrganizationIdAndDateRange(eq(10L), any(), any()))
                .thenReturn(List.of(a1, a2, a3));

        List<AbsenceStatsDTO> stats = absenceService.getMusicianAbsenceStats(10L, LocalDate.now().minusMonths(1), LocalDate.now(), adminUserDetails);
        assertEquals(1, stats.size());
        AbsenceStatsDTO s = stats.get(0);
        assertEquals(100L, s.getMusicianId());
        assertEquals(3, s.getTotalAbsences());
        assertEquals(1, s.getJustifiedCount());
        assertEquals(1, s.getUnjustifiedCount());
        assertEquals(1, s.getPendingCount());
    }

    @Test
    void updateAbsenceStatus_success() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        Absence a = Absence.builder()
                .id(42L)
                .organization(org)
                .musician(musician)
                .status(AbsenceStatus.PENDING)
                .build();
        when(absenceRepository.findById(42L)).thenReturn(Optional.of(a));
        when(absenceRepository.save(any(Absence.class))).thenAnswer(inv -> inv.getArgument(0));

        AbsenceDTO result = absenceService.updateAbsenceStatus(10L, 42L, new UpdateAbsenceStatusRequest(AbsenceStatus.JUSTIFIED), adminUserDetails);
        assertEquals(AbsenceStatus.JUSTIFIED, result.getStatus());
    }

    @Test
    void notifyEventCancellation_sendsNotificationsToMusiciansWithAccounts() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(musicianRepository.findAllByOrganizationId(10L)).thenReturn(List.of(musician));

        CancelEventNotificationRequest req = CancelEventNotificationRequest.builder()
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.of(2026, 11, 20))
                .message("Salle indisponible")
                .build();

        absenceService.notifyEventCancellation(10L, req, adminUserDetails);

        verify(notificationService).createNotification(
                eq(musicianUser),
                eq("EVENT_CANCELLED"),
                contains("Annulation"),
                contains("Salle indisponible"),
                anyString(),
                eq("ORGANIZATION"),
                eq(10L)
        );
    }
}
