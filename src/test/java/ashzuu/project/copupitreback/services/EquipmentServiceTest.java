package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.EquipmentRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void testGetAllRequests() {
        EquipmentRequest r1 = EquipmentRequest.builder().id(1L).title("Req 1").build();
        when(equipmentRepository.findAll()).thenReturn(List.of(r1));

        List<EquipmentRequestDTO> result = equipmentService.getAllRequests();
        assertEquals(1, result.size());
        assertEquals("Req 1", result.get(0).getTitle());
    }

    @Test
    void testGetMyEquipmentRequests() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).name("Harmonie").build();
        EquipmentRequest req = EquipmentRequest.builder().id(1L).organization(org).title("Timbales").build();

        when(organizationRepository.getAllByUserId(1L)).thenReturn(List.of(org));
        when(equipmentRepository.findByOrganizationIdIn(List.of(10L))).thenReturn(List.of(req));

        List<EquipmentRequestDTO> result = equipmentService.getMyEquipmentRequests(details);
        assertEquals(1, result.size());
        assertEquals("Timbales", result.get(0).getTitle());
    }

    @Test
    void testGetMyEquipmentRequestsNoOrgs() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        when(organizationRepository.getAllByUserId(1L)).thenReturn(List.of());

        List<EquipmentRequestDTO> result = equipmentService.getMyEquipmentRequests(details);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRequestsByOrganization() {
        EquipmentRequest req = EquipmentRequest.builder().id(1L).title("Pupitres").build();
        when(equipmentRepository.findByOrganizationId(10L)).thenReturn(List.of(req));

        List<EquipmentRequestDTO> result = equipmentService.getRequestsByOrganization(10L);
        assertEquals(1, result.size());
        assertEquals("Pupitres", result.get(0).getTitle());
    }

    @Test
    void testSaveRequestUnauthenticated() {
        EquipmentRequestDTO dto = new EquipmentRequestDTO();
        assertThrows(AccessDeniedException.class, () -> equipmentService.saveRequest(dto, null));
    }

    @Test
    void testSaveRequestMissingFields() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());

        // Missing title
        EquipmentRequestDTO dtoNoTitle = EquipmentRequestDTO.builder().equipmentName("Timbales").duration("1 jour").build();
        assertThrows(IllegalArgumentException.class, () -> equipmentService.saveRequest(dtoNoTitle, details));

        // Missing equipmentName
        EquipmentRequestDTO dtoNoEquipment = EquipmentRequestDTO.builder().title("Prêt").duration("1 jour").build();
        assertThrows(IllegalArgumentException.class, () -> equipmentService.saveRequest(dtoNoEquipment, details));

        // Missing duration
        EquipmentRequestDTO dtoNoDuration = EquipmentRequestDTO.builder().title("Prêt").equipmentName("Timbales").build();
        assertThrows(IllegalArgumentException.class, () -> equipmentService.saveRequest(dtoNoDuration, details));
    }

    @Test
    void testSaveRequestCreateNoOrgId() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .title("Title")
                .equipmentName("Timbales")
                .duration("1 week-end")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipmentService.saveRequest(dto, details));
        assertTrue(ex.getMessage().contains("L'identifiant de l'organisation est obligatoire"));
    }

    @Test
    void testSaveRequestCreateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .title("Title")
                .equipmentName("Timbales")
                .duration("1 week-end")
                .organizationId(10L)
                .build();

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> equipmentService.saveRequest(dto, details));
    }

    @Test
    void testSaveRequestCreateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .title("Title")
                .equipmentName("Timbales")
                .duration("1 week-end")
                .organizationId(10L)
                .eventDates(List.of(LocalDate.of(2026, 7, 20)))
                .build();

        Organization org = Organization.builder().id(10L).name("Org name").build();

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));

        when(equipmentRepository.save(any(EquipmentRequest.class))).thenAnswer(inv -> {
            EquipmentRequest saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        EquipmentRequestDTO savedDto = equipmentService.saveRequest(dto, details);

        assertNotNull(savedDto);
        assertEquals(99L, savedDto.getId());
        assertEquals("OPEN", savedDto.getStatus());
        assertEquals(1, savedDto.getEventDates().size());
        verify(equipmentRepository).save(any(EquipmentRequest.class));
    }

    @Test
    void testSaveRequestUpdateNotFound() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .id(100L)
                .title("Title")
                .equipmentName("Timbales")
                .duration("1 week-end")
                .build();

        when(equipmentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> equipmentService.saveRequest(dto, details));
    }

    @Test
    void testSaveRequestUpdateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();
        EquipmentRequest request = EquipmentRequest.builder().id(100L).organization(org).build();
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .id(100L)
                .title("Title")
                .equipmentName("Timbales")
                .duration("1 week-end")
                .build();

        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> equipmentService.saveRequest(dto, details));
    }

    @Test
    void testSaveRequestUpdateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();

        EquipmentRequest request = EquipmentRequest.builder()
                .id(100L)
                .organization(org)
                .equipmentRequestDates(new ArrayList<>())
                .build();

        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .id(100L)
                .title("New Title")
                .equipmentName("New Name")
                .duration("1 week-end")
                .description("New Desc")
                .status("CLOSED")
                .organizationId(10L)
                .eventDates(List.of(LocalDate.of(2026, 7, 20)))
                .build();

        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(equipmentRepository.save(any(EquipmentRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentRequestDTO result = equipmentService.saveRequest(dto, details);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Name", result.getEquipmentName());
        assertEquals("CLOSED", result.getStatus());
        assertEquals(1, result.getEventDates().size());
        verify(equipmentRepository).save(any(EquipmentRequest.class));
    }

    @Test
    void testUpdateStatusSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).username("admin_user").build());
        Organization org = Organization.builder().id(10L).name("Harmonie").build();
        EquipmentRequest request = EquipmentRequest.builder()
                .id(50L)
                .organization(org)
                .equipmentName("Timbales")
                .status("OPEN")
                .build();

        when(equipmentRepository.findById(50L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(equipmentRepository.save(any(EquipmentRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentRequestDTO updated = equipmentService.updateStatus(50L, "MET", details);

        assertNotNull(updated);
        assertEquals("MET", updated.getStatus());
        verify(emailService).sendEquipmentRequestStatusUpdate(eq("admin_user"), eq("Harmonie"), eq("Timbales"), eq("MET"));
    }

    @Test
    void testUpdateStatusInvalidStatus() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();
        EquipmentRequest request = EquipmentRequest.builder().id(50L).organization(org).build();

        when(equipmentRepository.findById(50L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> equipmentService.updateStatus(50L, "INVALID_STATUS", details));
    }

    @Test
    void testDeleteRequestSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();
        EquipmentRequest request = EquipmentRequest.builder().id(50L).organization(org).build();

        when(equipmentRepository.findById(50L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);

        equipmentService.deleteRequest(50L, details);

        verify(equipmentRepository).delete(request);
    }
}
