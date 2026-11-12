package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.musician.AssociateMusicianUserRequest;
import ashzuu.project.copupitreback.dto.musician.CreateMusicianRequest;
import ashzuu.project.copupitreback.dto.musician.CsvImportResultDTO;
import ashzuu.project.copupitreback.dto.musician.MusicianDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.Musician;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.MusicianRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicianServiceTest {

    @Mock
    private MusicianRepository musicianRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MusicianService musicianService;

    private UserDetailsImpl adminUserDetails;
    private Organization org;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("adminUser").email("admin@copupitre.fr").role(ashzuu.project.copupitreback.jpa.entities.user.Role.USER).build();
        adminUserDetails = new UserDetailsImpl(user);

        org = Organization.builder()
                .id(10L)
                .name("Orchestre Symphonique")
                .city("Lyon")
                .build();
    }

    @Test
    void getOrganizationMusicians_unauthenticated_returnsPublicListWithMaskedEmail() {
        when(organizationRepository.existsById(10L)).thenReturn(true);
        Musician m = Musician.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@test.fr")
                .instrument("Clarinette")
                .build();
        when(musicianRepository.findAllByOrganizationId(10L)).thenReturn(List.of(m));

        List<MusicianDTO> dtos = musicianService.getOrganizationMusicians(10L, null);
        assertEquals(1, dtos.size());
        assertEquals("Jean", dtos.get(0).getFirstName());
        assertNull(dtos.get(0).getEmail());
    }

    @Test
    void getOrganizationMusicians_unknownOrg_throwsUnknownID() {
        when(organizationRepository.existsById(999L)).thenReturn(false);
        assertThrows(UnknownID.class, () -> musicianService.getOrganizationMusicians(999L, adminUserDetails));
    }

    @Test
    void getOrganizationMusicians_success() {
        when(organizationRepository.existsById(10L)).thenReturn(true);
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        Musician m = Musician.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean.dupont@test.fr")
                .instrument("Clarinette")
                .build();
        when(musicianRepository.findAllByOrganizationId(10L)).thenReturn(List.of(m));

        List<MusicianDTO> dtos = musicianService.getOrganizationMusicians(10L, adminUserDetails);
        assertEquals(1, dtos.size());
        assertEquals("Jean", dtos.get(0).getFirstName());
        assertEquals("Dupont", dtos.get(0).getLastName());
        assertEquals("jean.dupont@test.fr", dtos.get(0).getEmail());
    }

    @Test
    void addMusician_notAdmin_throwsAccessDenied() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);
        CreateMusicianRequest req = CreateMusicianRequest.builder()
                .firstName("Jean").lastName("Dupont").email("jean@test.fr").instrument("Flûte").build();
        assertThrows(AccessDeniedException.class, () -> musicianService.addMusician(10L, req, adminUserDetails));
    }

    @Test
    void addMusician_alreadyInOrg_throwsIllegalArgument() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        Musician existing = Musician.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean@test.fr")
                .organizations(new HashSet<>(Collections.singletonList(org)))
                .build();
        when(musicianRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase("Dupont", "Jean", "jean@test.fr"))
                .thenReturn(Optional.of(existing));

        CreateMusicianRequest req = CreateMusicianRequest.builder()
                .firstName("Jean").lastName("Dupont").email("jean@test.fr").instrument("Flûte").build();

        assertThrows(IllegalArgumentException.class, () -> musicianService.addMusician(10L, req, adminUserDetails));
    }

    @Test
    void addMusician_newMusician_successWithAutoLinkUser() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(musicianRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase("Dupont", "Jean", "jean@test.fr"))
                .thenReturn(Optional.empty());

        User linkedUser = User.builder().id(2L).username("jeandupont").email("jean@test.fr").build();
        when(userRepository.findByEmail("jean@test.fr")).thenReturn(Optional.of(linkedUser));

        when(musicianRepository.save(any(Musician.class))).thenAnswer(inv -> {
            Musician saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        CreateMusicianRequest req = CreateMusicianRequest.builder()
                .firstName("Jean").lastName("Dupont").email("jean@test.fr").instrument("Violoncelle").build();

        MusicianDTO result = musicianService.addMusician(10L, req, adminUserDetails);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Jean", result.getFirstName());
        assertEquals(2L, result.getUserId());
        assertEquals("jeandupont", result.getUsername());
    }

    @Test
    void importMusiciansCsv_validFileWithSkippedDuplicates() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        String csv = """
                Nom,Prénom,Email,Instrument
                Dupont,Jean,jean@test.fr,Hautbois
                Durand,Marie,marie@test.fr,Flûte
                Dupont,Jean,jean@test.fr,Hautbois
                Invalide,SansEmail,,Trompette
                """;

        when(musicianRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase("Dupont", "Jean", "jean@test.fr"))
                .thenReturn(Optional.empty());
        when(musicianRepository.findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase("Durand", "Marie", "marie@test.fr"))
                .thenReturn(Optional.empty());

        when(musicianRepository.save(any(Musician.class))).thenAnswer(inv -> inv.getArgument(0));

        CsvImportResultDTO result = musicianService.importMusiciansCsv(10L, csv, adminUserDetails);

        assertEquals(4, result.getTotalRows());
        assertEquals(2, result.getImportedCount());
        assertEquals(1, result.getSkippedCount()); // Double Dupont Jean dans le même fichier
        assertEquals(1, result.getErrorCount());   // Ligne sans email
        assertEquals(2, result.getMusicians().size());
    }

    @Test
    void removeMusician_success() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        Musician m = Musician.builder()
                .id(5L)
                .firstName("Marc")
                .lastName("Lemoine")
                .organizations(new HashSet<>(Collections.singletonList(org)))
                .build();
        when(musicianRepository.findById(5L)).thenReturn(Optional.of(m));

        musicianService.removeMusician(10L, 5L, adminUserDetails);

        assertTrue(m.getOrganizations().isEmpty());
        verify(musicianRepository).save(m);
    }

    @Test
    void associateUserByEmail_userNotFound_throwsUnknownID() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.existsById(10L)).thenReturn(true);

        Musician m = Musician.builder()
                .id(5L)
                .organizations(new HashSet<>(Collections.singletonList(org)))
                .build();
        when(musicianRepository.findById(5L)).thenReturn(Optional.of(m));
        when(userRepository.findByEmail("inexistant@test.fr")).thenReturn(Optional.empty());

        AssociateMusicianUserRequest req = new AssociateMusicianUserRequest("inexistant@test.fr");
        assertThrows(UnknownID.class, () -> musicianService.associateUserByEmail(10L, 5L, req, adminUserDetails));
    }

    @Test
    void associateUserByEmail_success() {
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.existsById(10L)).thenReturn(true);

        Musician m = Musician.builder()
                .id(5L)
                .firstName("Alice")
                .lastName("Bernard")
                .email("alice@test.fr")
                .organizations(new HashSet<>(Collections.singletonList(org)))
                .build();
        when(musicianRepository.findById(5L)).thenReturn(Optional.of(m));

        User user = User.builder().id(99L).username("aliceb").email("alice@test.fr").build();
        when(userRepository.findByEmail("alice@test.fr")).thenReturn(Optional.of(user));
        when(musicianRepository.save(any(Musician.class))).thenAnswer(inv -> inv.getArgument(0));

        AssociateMusicianUserRequest req = new AssociateMusicianUserRequest("alice@test.fr");
        MusicianDTO result = musicianService.associateUserByEmail(10L, 5L, req, adminUserDetails);

        assertNotNull(result);
        assertEquals(99L, result.getUserId());
        assertEquals("aliceb", result.getUsername());
    }
}
