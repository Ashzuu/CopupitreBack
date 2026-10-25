package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.log.ClientLogRequest;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.services.LogService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
@AutoConfigureMockMvc(addFilters = false)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private LogService logService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void testReceiveErrorLogAnonymous() throws Exception {
        ClientLogRequest request = ClientLogRequest.builder()
                .level("ERROR")
                .message("Une erreur est survenue lors du chargement des renforts")
                .context("ReinforcementsListPage")
                .url("/renforts")
                .stack("Error: network issue\n  at Component.load")
                .timestamp(Instant.now())
                .build();

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testReceiveWarnLog() throws Exception {
        ClientLogRequest request = ClientLogRequest.builder()
                .level("WARN")
                .message("Session expirante dans 2 minutes")
                .context("AuthService")
                .url("/dashboard")
                .timestamp(Instant.now())
                .build();

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testReceiveInfoLog() throws Exception {
        ClientLogRequest request = ClientLogRequest.builder()
                .level("INFO")
                .message("Changement d'onglet vers 'Mes demandes'")
                .context("PermanentRecruitmentsListPage")
                .url("/recrutements")
                .build();

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testReceiveLogBlankMessageBadRequest() throws Exception {
        ClientLogRequest request = ClientLogRequest.builder()
                .level("ERROR")
                .message("   ")
                .context("App")
                .build();

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
