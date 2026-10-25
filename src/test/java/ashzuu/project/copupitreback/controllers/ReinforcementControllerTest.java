package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.reinforcement.ApplyRequest;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.dto.reinforcement.UpdateApplicationStatusRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.ReinforcementService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReinforcementController.class)
class ReinforcementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReinforcementService reinforcementService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllUnauthenticated() throws Exception {
        mockMvc.perform(get("/reinforcements/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllAuthenticated() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ReinforcementAnnouncementDTO req = ReinforcementAnnouncementDTO.builder().id(10L).title("Need Trombone").build();
        when(reinforcementService.getAllAnnouncements()).thenReturn(List.of(req));

        mockMvc.perform(get("/reinforcements/all")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Need Trombone"));
    }

    @Test
    void testGetMyAnnouncements() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ReinforcementAnnouncementDTO req = ReinforcementAnnouncementDTO.builder().id(10L).title("My Org Need").build();
        when(reinforcementService.getMyReinforcements(any())).thenReturn(List.of(req));

        mockMvc.perform(get("/reinforcements/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("My Org Need"));
    }

    @Test
    void testSaveAnnouncement() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ReinforcementAnnouncementDTO inputDto = ReinforcementAnnouncementDTO.builder().title("New Req").organizationId(20L).build();
        ReinforcementAnnouncementDTO savedDto = ReinforcementAnnouncementDTO.builder().id(100L).title("New Req").build();

        when(reinforcementService.saveAnnouncement(any(ReinforcementAnnouncementDTO.class), any())).thenReturn(savedDto);

        mockMvc.perform(post("/reinforcements/save")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("New Req"));
    }

    @Test
    void testApplyToAnnouncement() throws Exception {
        User userEntity = User.builder().id(2L).username("candidat").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ApplyRequest request = ApplyRequest.builder().message("Dispo avec sax").build();
        ReinforcementResponseDTO responseDTO = ReinforcementResponseDTO.builder()
                .id(42L)
                .announcementId(10L)
                .respondentUsername("candidat")
                .status("PENDING")
                .message("Dispo avec sax")
                .build();

        when(reinforcementService.applyToAnnouncement(eq(10L), any(), eq("Dispo avec sax"))).thenReturn(responseDTO);

        mockMvc.perform(post("/reinforcements/10/apply")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.respondentUsername").value("candidat"));
    }

    @Test
    void testUpdateApplicationStatus() throws Exception {
        User userEntity = User.builder().id(1L).username("admin").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder().status("ACCEPTED").build();
        ReinforcementResponseDTO responseDTO = ReinforcementResponseDTO.builder()
                .id(42L)
                .announcementId(10L)
                .respondentUsername("candidat")
                .status("ACCEPTED")
                .build();

        when(reinforcementService.updateApplicationStatus(eq(10L), eq(42L), eq("ACCEPTED"), any())).thenReturn(responseDTO);

        mockMvc.perform(patch("/reinforcements/10/applications/42/status")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void testGetMyApplications() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        ReinforcementResponseDTO resp = ReinforcementResponseDTO.builder()
                .id(1L)
                .announcementId(10L)
                .status("PENDING")
                .respondentUsername("testuser")
                .build();

        when(reinforcementService.getMyApplications(any())).thenReturn(List.of(resp));

        mockMvc.perform(get("/reinforcements/my-applications")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].announcementId").value(10));
    }
}
