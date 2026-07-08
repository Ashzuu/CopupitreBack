package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
