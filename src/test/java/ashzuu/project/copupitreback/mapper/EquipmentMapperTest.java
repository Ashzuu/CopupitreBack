package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.equipment.EquipmentRequestDTO;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDate;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequestDateId;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentMapperTest {

    @Test
    void testToDtoNull() {
        assertNull(EquipmentMapper.toDto((EquipmentRequest) null));
    }

    @Test
    void testToDto() {
        Organization org = Organization.builder().id(10L).name("My Org").build();
        
        EquipmentRequest req = EquipmentRequest.builder()
                .id(1L)
                .title("Need stands")
                .equipmentName("Music Stands")
                .status("OPEN")
                .duration("3")
                .description("Need 5 music stands")
                .organization(org)
                .createdAt(Instant.now())
                .build();

        EquipmentRequestDateDateBuilder dateBuilder = new EquipmentRequestDateDateBuilder(req, LocalDate.of(2026, 7, 10));
        req.setEquipmentRequestDates(List.of(dateBuilder.build()));

        EquipmentRequestDTO dto = EquipmentMapper.toDto(req);

        assertNotNull(dto);
        assertEquals(req.getId(), dto.getId());
        assertEquals(req.getTitle(), dto.getTitle());
        assertEquals(req.getEquipmentName(), dto.getEquipmentName());
        assertEquals(req.getStatus(), dto.getStatus());
        assertEquals(req.getDuration(), dto.getDuration());
        assertEquals(req.getDescription(), dto.getDescription());
        assertEquals(10L, dto.getOrganizationId());
        assertEquals("My Org", dto.getOrganizationName());
        assertEquals(1, dto.getEventDates().size());
        assertEquals(LocalDate.of(2026, 7, 10), dto.getEventDates().get(0));
        assertEquals(req.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void testToDtoList() {
        EquipmentRequest req1 = EquipmentRequest.builder().id(1L).title("Req 1").build();
        EquipmentRequest req2 = EquipmentRequest.builder().id(2L).title("Req 2").build();

        List<EquipmentRequestDTO> dtos = EquipmentMapper.toDto(List.of(req1, req2));

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Req 1", dtos.get(0).getTitle());
        assertEquals("Req 2", dtos.get(1).getTitle());
    }

    @Test
    void testToEntityNull() {
        assertNull(EquipmentMapper.toEntity(null));
    }

    @Test
    void testToEntity() {
        EquipmentRequestDTO dto = EquipmentRequestDTO.builder()
                .id(1L)
                .title("Need stand")
                .equipmentName("Stands")
                .duration("2")
                .description("Desc")
                .status("OPEN")
                .createdAt(Instant.now())
                .build();

        EquipmentRequest entity = EquipmentMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getTitle(), entity.getTitle());
        assertEquals(dto.getEquipmentName(), entity.getEquipmentName());
        assertEquals(dto.getDuration(), entity.getDuration());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getStatus(), entity.getStatus());
        assertEquals(dto.getCreatedAt(), entity.getCreatedAt());
    }

    // Helper class to construct EquipmentRequestDate
    private static class EquipmentRequestDateDateBuilder {
        private final EquipmentRequest req;
        private final LocalDate date;

        public EquipmentRequestDateDateBuilder(EquipmentRequest req, LocalDate date) {
            this.req = req;
            this.date = date;
        }

        public EquipmentRequestDate build() {
            EquipmentRequestDate rd = new EquipmentRequestDate();
            EquipmentRequestDateId id = new EquipmentRequestDateId();
            id.setEventDate(date);
            id.setRequestId(req.getId());
            rd.setId(id);
            rd.setRequest(req);
            return rd;
        }
    }
}
