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
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReinforcementServiceTest {

    @Mock
    private ReinforcementRepository reinforcementRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private ReinforcementService reinforcementService;

    @Test
    void testGetAllAnnouncements() {
        ReinforcementRequest r = ReinforcementRequest.builder().id(1L).title("Violin").build();
        when(reinforcementRepository.findAll()).thenReturn(List.of(r));

        List<ReinforcementAnnouncementDTO> result = reinforcementService.getAllAnnouncements();
        assertEquals(1, result.size());
        assertEquals("Violin", result.get(0).getTitle());
    }

    @Test
    void testGetMyReinforcements() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        ReinforcementRequest r = ReinforcementRequest.builder().id(10L).title("Trompette").build();
        when(reinforcementRepository.findByAdminUserId(1L)).thenReturn(List.of(r));

        List<ReinforcementAnnouncementDTO> result = reinforcementService.getMyReinforcements(details);
        assertEquals(1, result.size());
        assertEquals("Trompette", result.get(0).getTitle());
    }

    @Test
    void testGetMyReinforcementsUnauthenticated() {
        assertThrows(AccessDeniedException.class, () -> reinforcementService.getMyReinforcements(null));
    }

    @Test
    void testSaveAnnouncementUnauthenticated() {
        ReinforcementAnnouncementDTO dto = new ReinforcementAnnouncementDTO();
        assertThrows(AccessDeniedException.class, () -> reinforcementService.saveAnnouncement(dto, null));
    }

    @Test
    void testSaveAnnouncementCreateNoOrgId() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder().title("Title").build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> reinforcementService.saveAnnouncement(dto, details));
        assertEquals("L'identifiant de l'organisation est obligatoire pour créer une demande", ex.getMessage());
    }

    @Test
    void testSaveAnnouncementCreateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder().title("Title").organizationId(10L).build();

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> reinforcementService.saveAnnouncement(dto, details));
    }

    @Test
    void testSaveAnnouncementCreateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder()
                .title("Title")
                .instrumentNeeded("Flute")
                .requiredQuantity(1)
                .organizationId(10L)
                .responses(List.of(ReinforcementResponseDTO.builder().respondentUsername("music_user").status("PENDING").build()))
                .build();

        Organization org = Organization.builder().id(10L).name("Brass Band").build();
        User musician = User.builder().username("music_user").build();

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("music_user")).thenReturn(Optional.of(musician));
        
        when(reinforcementRepository.save(any(ReinforcementRequest.class))).thenAnswer(inv -> {
            ReinforcementRequest saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        ReinforcementAnnouncementDTO result = reinforcementService.saveAnnouncement(dto, details);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals("OPEN", result.getStatus());
        assertEquals(1, result.getResponses().size());
        verify(emailService).sendNewAnnouncementAlert(anyString(), eq("Brass Band"), eq("Title"));
        verify(reinforcementRepository).save(any(ReinforcementRequest.class));
    }

    @Test
    void testSaveAnnouncementUpdateNotFound() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder().id(100L).build();

        when(reinforcementRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> reinforcementService.saveAnnouncement(dto, details));
    }

    @Test
    void testSaveAnnouncementUpdateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();
        ReinforcementRequest request = ReinforcementRequest.builder().id(100L).organization(org).build();
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder().id(100L).build();

        when(reinforcementRepository.findById(100L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> reinforcementService.saveAnnouncement(dto, details));
    }

    @Test
    void testSaveAnnouncementUpdateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();
        
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(100L)
                .organization(org)
                .applications(new ArrayList<>())
                .build();
        
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder()
                .id(100L)
                .title("New Title")
                .instrumentNeeded("Clarinet")
                .requiredQuantity(2)
                .status("CLOSED")
                .organizationId(10L)
                .responses(List.of(
                        ReinforcementResponseDTO.builder().id(200L).respondentUsername("music_user").status("ACCEPTED").build()
                ))
                .build();

        User musician = User.builder().username("music_user").build();

        when(reinforcementRepository.findById(100L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("music_user")).thenReturn(Optional.of(musician));
        when(reinforcementRepository.save(any(ReinforcementRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ReinforcementAnnouncementDTO result = reinforcementService.saveAnnouncement(dto, details);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("Clarinet", result.getInstrumentNeeded());
        assertEquals("CLOSED", result.getStatus());
        assertEquals(1, result.getResponses().size());
        verify(reinforcementRepository).save(any(ReinforcementRequest.class));
    }

    @Test
    void testApplyToAnnouncementSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(2L).username("candidat").build());
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(10L)
                .title("Cherche Saxophone")
                .status("OPEN")
                .build();
        User candidate = User.builder().id(2L).username("candidat").firstName("Luc").lastName("Dubois").build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(applicationRepository.existsByRequestIdAndUserId(10L, 2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(candidate));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(50L);
            return a;
        });

        ReinforcementResponseDTO response = reinforcementService.applyToAnnouncement(10L, details, "Disponible avec mon sax alto");

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("candidat", response.getRespondentUsername());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Disponible avec mon sax alto", response.getMessage());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void testApplyToAnnouncementClosedForbidden() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(2L).build());
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(10L)
                .status("CLOSED")
                .build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> reinforcementService.applyToAnnouncement(10L, details, "message"));
        assertTrue(ex.getMessage().contains("n'accepte plus de candidatures"));
    }

    @Test
    void testApplyToAnnouncementAlreadyAppliedForbidden() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(2L).build());
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(10L)
                .status("OPEN")
                .build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(applicationRepository.existsByRequestIdAndUserId(10L, 2L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reinforcementService.applyToAnnouncement(10L, details, "message"));
        assertEquals("Vous avez déjà postulé à cette annonce", ex.getMessage());
    }

    @Test
    void testApplyToAnnouncementAdminForbidden() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(2L).build());
        Organization org = Organization.builder().id(5L).build();
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(10L)
                .status("OPEN")
                .organization(org)
                .build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(2L, 5L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reinforcementService.applyToAnnouncement(10L, details, "message"));
        assertTrue(ex.getMessage().contains("Les administrateurs d'une organisation ne peuvent pas postuler"));
    }

    @Test
    void testUpdateApplicationStatusAccepted() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(5L).name("Harmonie de Lyon").build();
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(10L)
                .organization(org)
                .requiredQuantity(2)
                .status("OPEN")
                .build();
        User candidate = User.builder().id(3L).username("candidat_sax").email("candidat@test.fr").build();
        Application app = Application.builder()
                .id(100L)
                .request(request)
                .user(candidate)
                .status("PENDING")
                .build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 5L)).thenReturn(true);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.countByRequestIdAndStatus(10L, "ACCEPTED")).thenReturn(1L);

        ReinforcementResponseDTO result = reinforcementService.updateApplicationStatus(10L, 100L, "ACCEPTED", details);

        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());
        verify(emailService).sendApplicationStatusUpdate("candidat@test.fr", "candidat_sax", "ACCEPTED", "Harmonie de Lyon");
        assertEquals("OPEN", request.getStatus());
    }

    @Test
    void testUpdateApplicationStatusAcceptedObjectiveMet() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(5L).name("Harmonie de Lyon").build();
        ReinforcementRequest request = ReinforcementRequest.builder()
                .id(10L)
                .organization(org)
                .requiredQuantity(1)
                .status("OPEN")
                .build();
        User candidate = User.builder().id(3L).username("candidat_sax").email("candidat@test.fr").build();
        Application app = Application.builder()
                .id(100L)
                .request(request)
                .user(candidate)
                .status("PENDING")
                .build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 5L)).thenReturn(true);
        when(applicationRepository.findById(100L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.countByRequestIdAndStatus(10L, "ACCEPTED")).thenReturn(1L);

        ReinforcementResponseDTO result = reinforcementService.updateApplicationStatus(10L, 100L, "ACCEPTED", details);

        assertNotNull(result);
        assertEquals("ACCEPTED", result.getStatus());
        assertEquals("OBJECTIVE_MET", request.getStatus());
        verify(reinforcementRepository).save(request);
    }

    @Test
    void testUpdateApplicationStatusNotAdminForbidden() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(5L).build();
        ReinforcementRequest request = ReinforcementRequest.builder().id(10L).organization(org).build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 5L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> reinforcementService.updateApplicationStatus(10L, 100L, "ACCEPTED", details));
    }

    @Test
    void testGetMyApplications() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(2L).username("musician").build());
        Application app = Application.builder()
                .id(50L)
                .user(User.builder().id(2L).username("musician").build())
                .status("PENDING")
                .appliedAt(Instant.now())
                .build();

        when(applicationRepository.findByUserId(2L)).thenReturn(List.of(app));

        List<ReinforcementResponseDTO> result = reinforcementService.getMyApplications(details);
        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("musician", result.get(0).getRespondentUsername());
    }

    @Test
    void testGetMyApplicationsUnauthenticatedThrows() {
        assertThrows(AccessDeniedException.class, () -> reinforcementService.getMyApplications(null));
    }

    @Test
    void testGetAllResponsesAsAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).username("admin").build());
        Organization org = Organization.builder().id(5L).build();
        ReinforcementRequest request = ReinforcementRequest.builder().id(10L).organization(org).build();
        Application app = Application.builder().id(20L).request(request).status("PENDING").build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 5L)).thenReturn(true);
        when(applicationRepository.findAllByIdRequest(10L)).thenReturn(List.of(app));

        List<ReinforcementResponseDTO> responses = reinforcementService.getAllResponses(10L, details);
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void testGetAllResponsesNotAdminThrows() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(2L).username("other").build());
        Organization org = Organization.builder().id(5L).build();
        ReinforcementRequest request = ReinforcementRequest.builder().id(10L).organization(org).build();

        when(reinforcementRepository.findById(10L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(2L, 5L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> reinforcementService.getAllResponses(10L, details));
    }

    @Test
    void testGetAllResponsesUnauthenticatedThrows() {
        assertThrows(AccessDeniedException.class, () -> reinforcementService.getAllResponses(10L, null));
    }
}
