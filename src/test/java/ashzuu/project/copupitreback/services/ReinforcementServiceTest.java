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
import ashzuu.project.copupitreback.security.UserDetailsImpl;
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

        Organization org = Organization.builder().id(10L).name("Org name").build();
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
}
