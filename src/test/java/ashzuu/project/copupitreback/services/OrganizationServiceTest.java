package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.organization.OrganizationDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    void testGetAll() {
        Organization o = Organization.builder().id(1L).name("Org").build();
        when(organizationRepository.findAll()).thenReturn(List.of(o));

        List<OrganizationDTO> result = organizationService.getAll();
        assertEquals(1, result.size());
        assertEquals("Org", result.get(0).getName());
    }

    @Test
    void testGetByIdSuccess() {
        Organization o = Organization.builder().id(1L).name("Org").build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(o));

        OrganizationDTO result = organizationService.getById("1");
        assertNotNull(result);
        assertEquals("Org", result.getName());
    }

    @Test
    void testGetByIdNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> organizationService.getById("1"));
    }

    @Test
    void testGetOrganizationFromUser() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        Organization o = Organization.builder().id(1L).name("Org").build();
        when(organizationRepository.getAllByUserId(5L)).thenReturn(List.of(o));

        List<OrganizationDTO> result = organizationService.getOrganizationFromUser(details);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAnnouncementsOnOrganizationSuccess() {
        Organization o = Organization.builder().id(1L).reinforcementRequests(new ArrayList<>()).build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(o));

        var result = organizationService.getAnnouncementsOnOrganization("1");
        assertNotNull(result);
    }

    @Test
    void testGetAnnouncementsOnOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnknownID.class, () -> organizationService.getAnnouncementsOnOrganization("1"));
    }

    @Test
    void testGetEquipmentRequestOnOrganizationSuccess() {
        Organization o = Organization.builder().id(1L).equipmentRequests(new ArrayList<>()).build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(o));

        var result = organizationService.getEquipmentRequestOnOrganization("1");
        assertNotNull(result);
    }

    @Test
    void testGetEquipmentRequestOnOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnknownID.class, () -> organizationService.getEquipmentRequestOnOrganization("1"));
    }

    @Test
    void testSaveCreateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        User adminUser = User.builder().id(5L).build();
        OrganizationDTO dto = OrganizationDTO.builder().name("New Org").build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(adminUser));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        OrganizationDTO result = organizationService.save(dto, details);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("New Org", result.getName());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void testSaveCreateAdminNotFound() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        OrganizationDTO dto = OrganizationDTO.builder().name("New Org").build();

        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> organizationService.save(dto, details));
    }

    @Test
    void testSaveUpdateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        Organization org = Organization.builder()
                .id(1L)
                .name("Old Org")
                .concerts(new ArrayList<>())
                .annualNeeds(new ArrayList<>())
                .rehearsalManuals(new ArrayList<>())
                .rehearsalSchedules(new ArrayList<>())
                .build();
        OrganizationDTO dto = OrganizationDTO.builder()
                .id(1L)
                .name("Updated Org")
                .build();

        when(organizationRepository.isAdminOfOrganization(5L, 1L)).thenReturn(true);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationDTO result = organizationService.save(dto, details);

        assertNotNull(result);
        assertEquals("Updated Org", result.getName());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void testSaveUpdateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        OrganizationDTO dto = OrganizationDTO.builder().id(1L).build();

        when(organizationRepository.isAdminOfOrganization(5L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> organizationService.save(dto, details));
    }
}
