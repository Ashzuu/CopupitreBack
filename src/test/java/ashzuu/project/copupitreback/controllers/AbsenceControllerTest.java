package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.absence.*;
import ashzuu.project.copupitreback.jpa.entities.organization.AbsenceStatus;
import ashzuu.project.copupitreback.jpa.entities.organization.EventType;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtAuthenticationFilter;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.SecurityConfig;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.AbsenceService;
import ashzuu.project.copupitreback.services.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AbsenceController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AbsenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbsenceService absenceService;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailsImpl adminUserDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("admin").email("admin@test.fr").role(Role.USER).build();
        adminUserDetails = new UserDetailsImpl(user);

        Organization org = Organization.builder().id(10L).name("Philharmonie").build();
        when(organizationService.findOrganizationByIdOrName("10")).thenReturn(org);
    }

    @Test
    void registerAbsence_unauthenticated_returns401() throws Exception {
        CreateAbsenceRequest req = CreateAbsenceRequest.builder()
                .musicianId(1L)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.now().plusDays(2))
                .reason("Test")
                .build();

        mockMvc.perform(post("/organizations/10/absences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAbsence_authenticated_returns201() throws Exception {
        CreateAbsenceRequest req = CreateAbsenceRequest.builder()
                .musicianId(1L)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.now().plusDays(2))
                .reason("Voyage")
                .build();

        AbsenceDTO created = AbsenceDTO.builder()
                .id(99L)
                .musicianId(1L)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.now().plusDays(2))
                .reason("Voyage")
                .status(AbsenceStatus.PENDING)
                .build();

        when(absenceService.registerAbsence(eq(10L), any(CreateAbsenceRequest.class), any())).thenReturn(created);

        mockMvc.perform(post("/organizations/10/absences")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getAbsences_returnsList() throws Exception {
        AbsenceDTO a = AbsenceDTO.builder()
                .id(1L)
                .musicianFirstName("Sophie")
                .eventType(EventType.CONCERT)
                .status(AbsenceStatus.JUSTIFIED)
                .build();
        when(absenceService.getAbsences(eq(10L), any(), any(), any(), any(), any(), any())).thenReturn(List.of(a));

        mockMvc.perform(get("/organizations/10/absences")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].musicianFirstName").value("Sophie"));
    }

    @Test
    void getMusicianAbsenceStats_returnsStats() throws Exception {
        AbsenceStatsDTO stat = AbsenceStatsDTO.builder()
                .musicianId(5L)
                .musicianName("Sophie Martin")
                .totalAbsences(2)
                .justifiedCount(2)
                .build();
        when(absenceService.getMusicianAbsenceStats(eq(10L), any(), any(), any())).thenReturn(List.of(stat));

        mockMvc.perform(get("/organizations/10/absences/stats")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].musicianName").value("Sophie Martin"))
                .andExpect(jsonPath("$[0].totalAbsences").value(2));
    }

    @Test
    void updateAbsenceStatus_returnsUpdated() throws Exception {
        UpdateAbsenceStatusRequest req = new UpdateAbsenceStatusRequest(AbsenceStatus.JUSTIFIED);
        AbsenceDTO updated = AbsenceDTO.builder().id(12L).status(AbsenceStatus.JUSTIFIED).build();
        when(absenceService.updateAbsenceStatus(eq(10L), eq(12L), any(), any())).thenReturn(updated);

        mockMvc.perform(patch("/organizations/10/absences/12/status")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("JUSTIFIED"));
    }

    @Test
    void notifyEventCancellation_returns200() throws Exception {
        CancelEventNotificationRequest req = CancelEventNotificationRequest.builder()
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.now().plusDays(3))
                .message("Intempéries")
                .build();

        doNothing().when(absenceService).notifyEventCancellation(eq(10L), any(), any());

        mockMvc.perform(post("/organizations/10/absences/cancel-event")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
