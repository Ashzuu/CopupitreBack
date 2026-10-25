package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeedApplication;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeeds;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.AnnualNeedApplicationRepository;
import ashzuu.project.copupitreback.jpa.repositories.AnnualNeedRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnualNeedServiceTest {

    @Mock
    private AnnualNeedRepository repo;

    @Mock
    private AnnualNeedApplicationRepository applicationRepo;

    @Mock
    private OrganizationRepository orgRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AnnualNeedService service;

    private UserDetailsImpl userDetails;
    private User user;
    private Organization organization;
    private AnnualNeeds annualNeed;

    @BeforeEach
    void setUp() {
        user = User.builder().id(10L).username("johndoe").firstName("John").lastName("Doe").email("john@test.com").role(Role.USER).build();
        userDetails = new UserDetailsImpl(user);

        organization = Organization.builder()
                .id(1L)
                .name("Harmonie de Lyon")
                .city("Lyon")
                .formationType("Harmonie")
                .musicianCount(40)
                .description("Orchestre d'harmonie")
                .build();

        annualNeed = AnnualNeeds.builder()
                .id(100L)
                .instrument("Trompette")
                .requiredCount(2)
                .description("Pupitre convivial")
                .organization(organization)
                .applications(new ArrayList<>())
                .build();
    }

    @Test
    void testGetAllPermanentAnnouncements() {
        when(repo.findAllWithOrganization()).thenReturn(List.of(annualNeed));

        List<PermanentAnnouncementDTO> dtos = service.getAllPermanentAnnouncements();

        assertEquals(1, dtos.size());
        assertEquals("Trompette", dtos.get(0).getInstrument());
        assertEquals("Harmonie de Lyon", dtos.get(0).getOrganizationName());
        assertEquals("Lyon", dtos.get(0).getOrganizationCity());
        assertEquals("PERMANENT", dtos.get(0).getType());
    }

    @Test
    void testGetMyPermanentAnnouncements() {
        when(repo.findByAdminUserId(10L)).thenReturn(List.of(annualNeed));

        List<PermanentAnnouncementDTO> dtos = service.getMyPermanentAnnouncements(userDetails);

        assertEquals(1, dtos.size());
        assertEquals(100L, dtos.get(0).getId());
    }

    @Test
    void testApplyToAnnualNeedSuccess() {
        when(repo.findById(100L)).thenReturn(Optional.of(annualNeed));
        when(orgRepo.isAdminOfOrganization(10L, 1L)).thenReturn(false);
        when(applicationRepo.existsByAnnualNeedIdAndUserId(100L, 10L)).thenReturn(false);
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));

        AnnualNeedApplication savedApp = AnnualNeedApplication.builder()
                .id(50L)
                .annualNeed(annualNeed)
                .user(user)
                .status("PENDING")
                .appliedAt(Instant.now())
                .message("Je suis très motivé")
                .build();
        when(applicationRepo.save(any(AnnualNeedApplication.class))).thenReturn(savedApp);

        ReinforcementResponseDTO result = service.applyToAnnualNeed(100L, userDetails, "Je suis très motivé");

        assertNotNull(result);
        assertEquals(50L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Je suis très motivé", result.getMessage());
        assertEquals("johndoe", result.getRespondentUsername());
    }

    @Test
    void testApplyToAnnualNeedAdminForbidden() {
        when(repo.findById(100L)).thenReturn(Optional.of(annualNeed));
        when(orgRepo.isAdminOfOrganization(10L, 1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyToAnnualNeed(100L, userDetails, "Motivé")
        );
        assertTrue(ex.getMessage().contains("Les administrateurs d'une organisation ne peuvent pas postuler"));
        verify(applicationRepo, never()).save(any());
    }

    @Test
    void testApplyToAnnualNeedAlreadyApplied() {
        when(repo.findById(100L)).thenReturn(Optional.of(annualNeed));
        when(orgRepo.isAdminOfOrganization(10L, 1L)).thenReturn(false);
        when(applicationRepo.existsByAnnualNeedIdAndUserId(100L, 10L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.applyToAnnualNeed(100L, userDetails, "Encore moi")
        );
        assertTrue(ex.getMessage().contains("Vous avez déjà postulé"));
    }

    @Test
    void testUpdateApplicationStatusSuccess() {
        when(repo.findById(100L)).thenReturn(Optional.of(annualNeed));
        when(orgRepo.isAdminOfOrganization(10L, 1L)).thenReturn(true);

        AnnualNeedApplication app = AnnualNeedApplication.builder()
                .id(50L)
                .annualNeed(annualNeed)
                .user(user)
                .status("PENDING")
                .build();
        when(applicationRepo.findById(50L)).thenReturn(Optional.of(app));
        when(applicationRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReinforcementResponseDTO result = service.updateApplicationStatus(100L, 50L, "ACCEPTED", userDetails);

        assertEquals("ACCEPTED", result.getStatus());
        verify(emailService).sendApplicationStatusUpdate(eq("john@test.com"), eq("johndoe"), eq("ACCEPTED"), eq("Harmonie de Lyon"));
    }

    @Test
    void testUpdateApplicationStatusNotAdmin() {
        when(repo.findById(100L)).thenReturn(Optional.of(annualNeed));
        when(orgRepo.isAdminOfOrganization(10L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                service.updateApplicationStatus(100L, 50L, "ACCEPTED", userDetails)
        );
    }
}
