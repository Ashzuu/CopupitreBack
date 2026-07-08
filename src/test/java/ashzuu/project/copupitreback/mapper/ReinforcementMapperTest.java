package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReinforcementMapperTest {

    @Test
    void testToAnnouncementDtoNull() {
        assertNull(ReinforcementMapper.toAnnouncementDto((ReinforcementRequest) null));
    }

    @Test
    void testToAnnouncementDto() {
        Organization org = Organization.builder().id(5L).name("Test Orchestra").build();
        User user = User.builder().username("trombonist").build();

        ReinforcementRequest req = ReinforcementRequest.builder()
                .id(1L)
                .title("Need Trombone")
                .description("We need a trombone player")
                .deadline(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .requiredQuantity(1)
                .eventLocation("Paris")
                .eventDate(Instant.now().plusSeconds(7200))
                .repertoireDetails("Mahler 5")
                .instrumentNeeded("Trombone")
                .organization(org)
                .status("OPEN")
                .build();

        Application app1 = new Application();
        app1.setId(10L);
        app1.setRequest(req);
        app1.setUser(user);
        app1.setStatus("ACCEPTED");
        app1.setAppliedAt(Instant.now());

        Application app2 = new Application();
        app2.setId(11L);
        app2.setRequest(req);
        app2.setUser(user);
        app2.setStatus("PENDING");
        app2.setAppliedAt(Instant.now());

        req.setApplications(List.of(app1, app2));

        ReinforcementAnnouncementDTO dto = ReinforcementMapper.toAnnouncementDto(req);

        assertNotNull(dto);
        assertEquals(req.getId(), dto.getId());
        assertEquals(req.getTitle(), dto.getTitle());
        assertEquals(req.getDescription(), dto.getDescription());
        assertEquals(req.getDeadline(), dto.getDeadline());
        assertEquals(req.getCreatedAt(), dto.getCreatedAt());
        assertEquals(req.getRequiredQuantity(), dto.getRequiredQuantity());
        assertEquals(req.getEventLocation(), dto.getEventLocation());
        assertEquals(req.getEventDate(), dto.getEventDate());
        assertEquals(req.getRepertoireDetails(), dto.getRepertoireDetails());
        assertEquals(req.getInstrumentNeeded(), dto.getInstrumentNeeded());
        assertEquals("Test Orchestra", dto.getOrganizationName());
        assertEquals(5L, dto.getOrganizationId());
        assertEquals("OPEN", dto.getStatus());
        assertEquals(1, dto.getValidatedCount()); // only app1 is ACCEPTED
        assertEquals(2, dto.getResponses().size());
    }

    @Test
    void testToAnnouncementDtoList() {
        ReinforcementRequest req1 = ReinforcementRequest.builder().id(1L).title("Title 1").build();
        ReinforcementRequest req2 = ReinforcementRequest.builder().id(2L).title("Title 2").build();

        List<ReinforcementAnnouncementDTO> dtos = ReinforcementMapper.toAnnouncementDto(List.of(req1, req2));

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Title 1", dtos.get(0).getTitle());
        assertEquals("Title 2", dtos.get(1).getTitle());
    }

    @Test
    void testToEntityNull() {
        assertNull(ReinforcementMapper.toEntity((ReinforcementAnnouncementDTO) null));
    }

    @Test
    void testToEntity() {
        ReinforcementAnnouncementDTO dto = ReinforcementAnnouncementDTO.builder()
                .id(1L)
                .title("Trombone Needed")
                .instrumentNeeded("Trombone")
                .requiredQuantity(2)
                .eventDate(Instant.now().plusSeconds(7200))
                .eventLocation("Lyon")
                .repertoireDetails("Beethoven 9")
                .description("Long description")
                .deadline(Instant.now())
                .status("OPEN")
                .createdAt(Instant.now())
                .build();

        ReinforcementRequest entity = ReinforcementMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getTitle(), entity.getTitle());
        assertEquals(dto.getInstrumentNeeded(), entity.getInstrumentNeeded());
        assertEquals(dto.getRequiredQuantity(), entity.getRequiredQuantity());
        assertEquals(dto.getEventDate(), entity.getEventDate());
        assertEquals(dto.getEventLocation(), entity.getEventLocation());
        assertEquals(dto.getRepertoireDetails(), entity.getRepertoireDetails());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getDeadline(), entity.getDeadline());
        assertEquals(dto.getStatus(), entity.getStatus());
        assertEquals(dto.getCreatedAt(), entity.getCreatedAt());
        assertNotNull(entity.getApplications());
        assertTrue(entity.getApplications().isEmpty());
    }

    @Test
    void testToResponseDtoNull() {
        assertNull(ReinforcementMapper.toResponseDto((Application) null));
    }

    @Test
    void testToResponseDto() {
        ReinforcementRequest req = ReinforcementRequest.builder().id(1L).title("Req Title").build();
        User user = User.builder().username("jane_doe").build();
        
        Application app = new Application();
        app.setId(10L);
        app.setRequest(req);
        app.setUser(user);
        app.setStatus("ACCEPTED");
        app.setAppliedAt(Instant.now());

        ReinforcementResponseDTO dto = ReinforcementMapper.toResponseDto(app);

        assertNotNull(dto);
        assertEquals(app.getId(), dto.getId());
        assertEquals(1L, dto.getAnnouncementId());
        assertEquals("Req Title", dto.getAnnouncementTitle());
        assertEquals("jane_doe", dto.getRespondentUsername());
        assertEquals(app.getAppliedAt(), dto.getRespondedAt());
        assertEquals("ACCEPTED", dto.getStatus());
    }

    @Test
    void testToResponseEntityNull() {
        assertNull(ReinforcementMapper.toEntity((ReinforcementResponseDTO) null));
    }

    @Test
    void testToResponseEntity() {
        ReinforcementResponseDTO dto = ReinforcementResponseDTO.builder()
                .id(10L)
                .status("ACCEPTED")
                .respondedAt(Instant.now())
                .build();

        Application app = ReinforcementMapper.toEntity(dto);

        assertNotNull(app);
        assertEquals(dto.getId(), app.getId());
        assertEquals(dto.getStatus(), app.getStatus());
        assertEquals(dto.getRespondedAt(), app.getAppliedAt());
    }
}
