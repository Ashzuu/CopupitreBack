package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.musician.AssociateMusicianUserRequest;
import ashzuu.project.copupitreback.dto.musician.CreateMusicianRequest;
import ashzuu.project.copupitreback.dto.musician.CsvImportResultDTO;
import ashzuu.project.copupitreback.dto.musician.MusicianDTO;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtAuthenticationFilter;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.SecurityConfig;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.MusicianService;
import ashzuu.project.copupitreback.services.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MusicianController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class MusicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicianService musicianService;

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
    void getMusicians_unauthenticated_returns200Public() throws Exception {
        MusicianDTO m = MusicianDTO.builder().id(1L).firstName("Luc").lastName("Martin").email(null).instrument("Violon").build();
        when(musicianService.getOrganizationMusicians(eq(10L), any())).thenReturn(List.of(m));

        mockMvc.perform(get("/organizations/10/musicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Luc"))
                .andExpect(jsonPath("$[0].email").doesNotExist());
    }

    @Test
    void getMusicians_authenticated_returnsList() throws Exception {
        MusicianDTO m = MusicianDTO.builder().id(1L).firstName("Luc").lastName("Martin").email("luc@test.fr").instrument("Violon").build();
        when(musicianService.getOrganizationMusicians(eq(10L), any())).thenReturn(List.of(m));

        mockMvc.perform(get("/organizations/10/musicians")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Luc"))
                .andExpect(jsonPath("$[0].instrument").value("Violon"));
    }

    @Test
    void addMusician_valid_returns201() throws Exception {
        CreateMusicianRequest req = CreateMusicianRequest.builder()
                .firstName("Luc")
                .lastName("Martin")
                .email("luc@test.fr")
                .instrument("Violon")
                .build();

        MusicianDTO created = MusicianDTO.builder().id(1L).firstName("Luc").lastName("Martin").email("luc@test.fr").instrument("Violon").build();
        when(musicianService.addMusician(eq(10L), any(CreateMusicianRequest.class), any())).thenReturn(created);

        mockMvc.perform(post("/organizations/10/musicians")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Luc"));
    }

    @Test
    void importMusiciansCsvFile_returnsResult() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "musicians.csv", "text/csv", "Nom,Prénom,Email,Instrument\nDurand,Paul,paul@test.fr,Trompette".getBytes()
        );

        CsvImportResultDTO result = CsvImportResultDTO.builder()
                .totalRows(1)
                .importedCount(1)
                .skippedCount(0)
                .errorCount(0)
                .build();
        when(musicianService.importMusiciansCsv(eq(10L), any(String.class), any())).thenReturn(result);

        mockMvc.perform(multipart("/organizations/10/musicians/import-csv")
                        .file(file)
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1));
    }

    @Test
    void removeMusician_returns204() throws Exception {
        doNothing().when(musicianService).removeMusician(eq(10L), eq(5L), any());

        mockMvc.perform(delete("/organizations/10/musicians/5")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void associateUser_valid_returns200() throws Exception {
        AssociateMusicianUserRequest req = new AssociateMusicianUserRequest("paul@test.fr");
        MusicianDTO updated = MusicianDTO.builder().id(5L).userId(20L).username("paul20").build();
        when(musicianService.associateUserByEmail(eq(10L), eq(5L), any(), any())).thenReturn(updated);

        mockMvc.perform(post("/organizations/10/musicians/5/associate-user")
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(20L))
                .andExpect(jsonPath("$.username").value("paul20"));
    }
}
