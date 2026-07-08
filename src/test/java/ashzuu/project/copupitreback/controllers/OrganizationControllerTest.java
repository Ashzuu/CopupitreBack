package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.organization.OrganizationDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.OrganizationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllUnauthenticated() throws Exception {
        mockMvc.perform(get("/organizations/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllAuthenticated() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        OrganizationDTO org = OrganizationDTO.builder().id(10L).name("Philharmonie").build();
        when(organizationService.getAll()).thenReturn(List.of(org));

        mockMvc.perform(get("/organizations/all")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Philharmonie"));
    }

    @Test
    void testGetOrganizationById() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        OrganizationDTO org = OrganizationDTO.builder().id(10L).name("Philharmonie").build();
        when(organizationService.getById("10")).thenReturn(org);

        mockMvc.perform(get("/organizations/10")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Philharmonie"));
    }

    @Test
    void testGetOrganizationsMe() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        OrganizationDTO org = OrganizationDTO.builder().id(10L).name("My Org").build();
        when(organizationService.getOrganizationFromUser(any())).thenReturn(List.of(org));

        mockMvc.perform(get("/organizations/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Org"));
    }

    @Test
    void testGetAnnouncementsOnOrganization() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ReinforcementAnnouncementDTO ann = ReinforcementAnnouncementDTO.builder().id(50L).title("Need Flute").build();
        when(organizationService.getAnnouncementsOnOrganization("10")).thenReturn(List.of(ann));

        mockMvc.perform(get("/organizations/10/reinforcements")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Need Flute"));
    }

    @Test
    void testGetEquipmentRequestOnOrganization() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        EquipmentRequestDTO eq = EquipmentRequestDTO.builder().id(50L).title("Stands").build();
        when(organizationService.getEquipmentRequestOnOrganization("10")).thenReturn(List.of(eq));

        mockMvc.perform(get("/organizations/10/equipment-requests")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Stands"));
    }

    @Test
    void testCreateOrganization() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        OrganizationDTO inputDto = OrganizationDTO.builder().name("New Org").build();
        OrganizationDTO savedDto = OrganizationDTO.builder().id(100L).name("New Org").build();

        when(organizationService.save(any(OrganizationDTO.class), any())).thenReturn(savedDto);

        mockMvc.perform(post("/organizations/save")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("New Org"));
    }
}
