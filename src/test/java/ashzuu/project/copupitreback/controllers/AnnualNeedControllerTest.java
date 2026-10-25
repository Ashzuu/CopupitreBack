package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ApplyRequest;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.dto.reinforcement.UpdateApplicationStatusRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.AnnualNeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnnualNeedController.class)
class AnnualNeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnnualNeedService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailsImpl createUserDetails(Long id, String username) {
        User user = User.builder().id(id).username(username).role(Role.USER).build();
        return new UserDetailsImpl(user);
    }

    @Test
    void testGetAllUnauthenticated() throws Exception {
        mockMvc.perform(get("/annual-needs/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllAuthenticated() throws Exception {
        PermanentAnnouncementDTO dto = PermanentAnnouncementDTO.builder()
                .id(1L)
                .instrument("Trompette")
                .organizationName("Harmonie de Lyon")
                .status("OPEN")
                .build();
        when(service.getAllPermanentAnnouncements()).thenReturn(List.of(dto));

        mockMvc.perform(get("/annual-needs/all")
                        .with(SecurityMockMvcRequestPostProcessors.user(createUserDetails(1L, "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].instrument").value("Trompette"))
                .andExpect(jsonPath("$[0].organizationName").value("Harmonie de Lyon"));
    }

    @Test
    void testGetMyAnnouncements() throws Exception {
        when(service.getMyPermanentAnnouncements(any())).thenReturn(List.of());

        mockMvc.perform(get("/annual-needs/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(createUserDetails(1L, "user"))))
                .andExpect(status().isOk());
    }

    @Test
    void testApplyToAnnualNeed() throws Exception {
        ReinforcementResponseDTO resp = ReinforcementResponseDTO.builder()
                .id(10L)
                .announcementId(1L)
                .status("PENDING")
                .build();
        when(service.applyToAnnualNeed(eq(1L), any(), any())).thenReturn(resp);

        ApplyRequest request = new ApplyRequest();
        request.setMessage("Je suis partant !");

        mockMvc.perform(post("/annual-needs/1/apply")
                        .with(SecurityMockMvcRequestPostProcessors.user(createUserDetails(1L, "user")))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testUpdateApplicationStatus() throws Exception {
        ReinforcementResponseDTO resp = ReinforcementResponseDTO.builder()
                .id(10L)
                .status("ACCEPTED")
                .build();
        when(service.updateApplicationStatus(eq(1L), eq(10L), eq("ACCEPTED"), any())).thenReturn(resp);

        UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest();
        request.setStatus("ACCEPTED");

        mockMvc.perform(patch("/annual-needs/1/applications/10/status")
                        .with(SecurityMockMvcRequestPostProcessors.user(createUserDetails(1L, "admin")))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void testGetAnnouncementsByOrganization() throws Exception {
        PermanentAnnouncementDTO dto = PermanentAnnouncementDTO.builder()
                .id(2L)
                .instrument("Clarinette")
                .organizationId(5L)
                .build();
        when(service.getPermanentAnnouncementsByOrganization(5L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/annual-needs/organization/5")
                        .with(SecurityMockMvcRequestPostProcessors.user(createUserDetails(1L, "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].instrument").value("Clarinette"));
    }
}
