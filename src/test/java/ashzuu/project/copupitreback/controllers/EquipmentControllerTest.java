package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.equipment.UpdateEquipmentStatusRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.EquipmentService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentService equipmentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailsImpl createUserDetails() {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        return new UserDetailsImpl(userEntity);
    }

    @Test
    void testGetAllUnauthenticated() throws Exception {
        mockMvc.perform(get("/equipments/requests/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllAuthenticated() throws Exception {
        UserDetailsImpl userDetails = createUserDetails();
        EquipmentRequestDTO req = EquipmentRequestDTO.builder().id(10L).title("Request stands").build();
        when(equipmentService.getAllRequests()).thenReturn(List.of(req));

        mockMvc.perform(get("/equipments/requests/all")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Request stands"));
    }

    @Test
    void testGetMyRequests() throws Exception {
        UserDetailsImpl userDetails = createUserDetails();
        EquipmentRequestDTO req = EquipmentRequestDTO.builder().id(10L).title("Mes timbales").build();
        when(equipmentService.getMyEquipmentRequests(any())).thenReturn(List.of(req));

        mockMvc.perform(get("/equipments/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Mes timbales"));
    }

    @Test
    void testGetRequestsByOrganization() throws Exception {
        UserDetailsImpl userDetails = createUserDetails();
        EquipmentRequestDTO req = EquipmentRequestDTO.builder().id(10L).title("Req Org").build();
        when(equipmentService.getRequestsByOrganization(5L)).thenReturn(List.of(req));

        mockMvc.perform(get("/equipments/organization/5")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Req Org"));
    }

    @Test
    void testSaveRequest() throws Exception {
        UserDetailsImpl userDetails = createUserDetails();
        EquipmentRequestDTO inputDto = EquipmentRequestDTO.builder()
                .title("New Req")
                .equipmentName("Timbales")
                .duration("1 week-end")
                .organizationId(20L)
                .build();
        EquipmentRequestDTO savedDto = EquipmentRequestDTO.builder().id(100L).title("New Req").build();

        when(equipmentService.saveRequest(any(EquipmentRequestDTO.class), any())).thenReturn(savedDto);

        mockMvc.perform(post("/equipments/save")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("New Req"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        UserDetailsImpl userDetails = createUserDetails();
        UpdateEquipmentStatusRequest request = new UpdateEquipmentStatusRequest("MET");
        EquipmentRequestDTO updatedDto = EquipmentRequestDTO.builder().id(100L).status("MET").build();

        when(equipmentService.updateStatus(eq(100L), eq("MET"), any())).thenReturn(updatedDto);

        mockMvc.perform(patch("/equipments/100/status")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MET"));
    }

    @Test
    void testDeleteRequest() throws Exception {
        UserDetailsImpl userDetails = createUserDetails();

        doNothing().when(equipmentService).deleteRequest(eq(100L), any());

        mockMvc.perform(delete("/equipments/100")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(equipmentService).deleteRequest(eq(100L), any());
    }
}
