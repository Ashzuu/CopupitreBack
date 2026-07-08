package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.EquipmentRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
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
    void testSaveRequestUnauthenticated() {
        EquipmentRequestDTO dto = new EquipmentRequestDTO();
        assertThrows(AccessDeniedException.class, () -> equipmentService.saveRequest(dto, null));
    }

    @Test
    void testSaveRequestCreateNoOrgId() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder().title("Title").build(); // No orgId

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> equipmentService.saveRequest(dto, details));
        assertEquals("L'identifiant de l'organisation est obligatoire pour créer une demande", ex.getMessage());
    }

    @Test
    void testSaveRequestCreateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder().title("Title").organizationId(10L).build();

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> equipmentService.saveRequest(dto, details));
    }

    @Test
    void testSaveRequestCreateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .title("Title")
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
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder().id(100L).build();

        when(equipmentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> equipmentService.saveRequest(dto, details));
    }

    @Test
    void testSaveRequestUpdateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(1L).build());
        Organization org = Organization.builder().id(10L).build();
        EquipmentRequest request = EquipmentRequest.builder().id(100L).organization(org).build();
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder().id(100L).build();

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
                .duration("1")
                .description("New Desc")
                .status("CLOSED")
                .organizationId(10L)
                .eventDates(List.of(LocalDate.of(2026, 7, 20)))
                .build();

        when(equipmentRepository.findById(100L)).thenReturn(Optional.of(request));
        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(equipmentRepository.save(any(EquipmentRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        EquipmentRequestDTO result = equipmentService.saveRequest(dto, details);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("New Name", result.getEquipmentName());
        assertEquals("CLOSED", result.getStatus());
        assertEquals(1, result.getEventDates().size());
        verify(equipmentRepository).save(any(EquipmentRequest.class));
    }
}
