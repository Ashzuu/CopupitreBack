package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.dto.organization.AddAdminRequest;
import ashzuu.project.copupitreback.dto.organization.OrganizationDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    void testGetAnnualNeedsOnOrganization() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO need =
                ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO.builder().id(30L).instrument("Violon").build();
        when(organizationService.getAnnualNeedsOnOrganization("10")).thenReturn(List.of(need));

        mockMvc.perform(get("/organizations/10/annual-needs")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instrument").value("Violon"));
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

    @Test
    void testAddAdminUnauthenticated() throws Exception {
        AddAdminRequest request = AddAdminRequest.builder().username("newadmin").build();

        mockMvc.perform(post("/organizations/10/admins")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAddAdminSuccess() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        AddAdminRequest request = AddAdminRequest.builder().username("newadmin").build();
        OrganizationDTO updatedOrg = OrganizationDTO.builder()
                .id(10L)
                .name("Philharmonie")
                .adminUsernames(List.of("testuser", "newadmin"))
                .build();

        when(organizationService.addAdmin(eq(10L), eq("newadmin"), any())).thenReturn(updatedOrg);

        mockMvc.perform(post("/organizations/10/admins")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.adminUsernames[1]").value("newadmin"));
    }

    @Test
    void testAddAdminInvalidBlankUsername() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        AddAdminRequest request = AddAdminRequest.builder().username("   ").build();

        mockMvc.perform(post("/organizations/10/admins")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddAdminForbiddenNonAdmin() throws Exception {
        User userEntity = User.builder().id(2L).username("nonadmin").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        AddAdminRequest request = AddAdminRequest.builder().username("target").build();

        when(organizationService.addAdmin(eq(10L), eq("target"), any()))
                .thenThrow(new AccessDeniedException("Vous n'êtes pas administrateur de cette organisation"));

        mockMvc.perform(post("/organizations/10/admins")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddAdminUserNotFound() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        AddAdminRequest request = AddAdminRequest.builder().username("unknownuser").build();

        when(organizationService.addAdmin(eq(10L), eq("unknownuser"), any()))
                .thenThrow(new UnknownID("Utilisateur introuvable : unknownuser"));

        mockMvc.perform(post("/organizations/10/admins")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddAdminAlreadyAdmin() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        AddAdminRequest request = AddAdminRequest.builder().username("existingadmin").build();

        when(organizationService.addAdmin(eq(10L), eq("existingadmin"), any()))
                .thenThrow(new IllegalArgumentException("L'utilisateur est déjà administrateur"));

        mockMvc.perform(post("/organizations/10/admins")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveAdminUnauthenticated() throws Exception {
        mockMvc.perform(delete("/organizations/10/admins/someadmin")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRemoveAdminSuccess() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        OrganizationDTO updatedOrg = OrganizationDTO.builder()
                .id(10L)
                .name("Philharmonie")
                .adminUsernames(List.of("testuser"))
                .build();

        when(organizationService.removeAdmin(eq(10L), eq("admintoremove"), any())).thenReturn(updatedOrg);

        mockMvc.perform(delete("/organizations/10/admins/admintoremove")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminUsernames.length()").value(1))
                .andExpect(jsonPath("$.adminUsernames[0]").value("testuser"));
    }

    @Test
    void testRemoveAdminForbiddenNonAdmin() throws Exception {
        User userEntity = User.builder().id(2L).username("nonadmin").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        when(organizationService.removeAdmin(eq(10L), eq("admintoremove"), any()))
                .thenThrow(new AccessDeniedException("Vous n'êtes pas administrateur"));

        mockMvc.perform(delete("/organizations/10/admins/admintoremove")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRemoveAdminSelfForbidden() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        when(organizationService.removeAdmin(eq(10L), eq("testuser"), any()))
                .thenThrow(new IllegalArgumentException("Vous ne pouvez pas vous retirer vous-même des administrateurs de l'organisation"));

        mockMvc.perform(delete("/organizations/10/admins/testuser")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveAdminLastAdminRejected() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        when(organizationService.removeAdmin(eq(10L), eq("otheradmin"), any()))
                .thenThrow(new IllegalStateException("Impossible de supprimer le dernier administrateur de l'organisation"));

        mockMvc.perform(delete("/organizations/10/admins/otheradmin")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveAdminUserNotFound() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        when(organizationService.removeAdmin(eq(10L), eq("ghost"), any()))
                .thenThrow(new UnknownID("Utilisateur introuvable"));

        mockMvc.perform(delete("/organizations/10/admins/ghost")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }
}
