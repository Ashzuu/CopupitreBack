package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.organization.*;
import ashzuu.project.copupitreback.jpa.entities.*;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationMapperTest {

    @Test
    void testToDtoNull() {
        assertNull(OrganizationMapper.toDto((Organization) null));
    }

    @Test
    void testToDto() {
        Organization org = Organization.builder()
                .id(1L)
                .name("Philharmonie")
                .city("Paris")
                .formationType("Symphony")
                .description("Grande formation")
                .concertDress("Black Suit")
                .logisticsInfo("Bring stands")
                .musicianCount(80)
                .rehearsalAddress("123 Street")
                .rehearsalSchedule("Wednesdays 8pm")
                .createdAt(Instant.now())
                .build();

        User user = User.builder().firstName("John").lastName("Doe").build();
        OrganizationAdmin admin = new OrganizationAdmin();
        admin.setUser(user);
        admin.setOrganization(org);
        org.setOrganizationAdmins(List.of(admin));

        Concert concert = Concert.builder()
                .id(10L)
                .title("Winter Concert")
                .eventDate(Instant.now())
                .location("Hall A")
                .description("Nice concert")
                .organization(org)
                .build();
        org.setConcerts(List.of(concert));

        AnnualNeeds need = AnnualNeeds.builder()
                .id(20L)
                .instrument("Oboe")
                .requiredCount(2)
                .description("Permanent position")
                .organization(org)
                .build();
        org.setAnnualNeeds(List.of(need));

        RehearsalManual manual = RehearsalManual.builder()
                .id(30L)
                .manualDate(LocalDate.of(2026, 11, 1))
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(21, 0))
                .location("Rehearsal Room 1")
                .organization(org)
                .build();
        org.setRehearsalManuals(List.of(manual));

        RehearsalSchedule schedule = RehearsalSchedule.builder()
                .id(40L)
                .dayOfWeek("3")
                .startTime(LocalTime.of(20, 0))
                .endTime(LocalTime.of(22, 0))
                .location("Rehearsal Room 2")
                .organization(org)
                .build();
        org.setRehearsalSchedules(List.of(schedule));

        OrganizationDTO dto = OrganizationMapper.toDto(org);

        assertNotNull(dto);
        assertEquals(org.getId(), dto.getId());
        assertEquals(org.getName(), dto.getName());
        assertEquals(org.getCity(), dto.getCity());
        assertEquals(org.getFormationType(), dto.getFormationType());
        assertEquals(org.getDescription(), dto.getDescription());
        assertEquals(org.getConcertDress(), dto.getConcertDress());
        assertEquals(org.getLogisticsInfo(), dto.getLogisticsInfo());
        assertEquals(org.getMusicianCount(), dto.getMusicianCount());
        assertEquals(org.getRehearsalAddress(), dto.getRehearsalAddress());
        assertEquals(org.getRehearsalSchedule(), dto.getRehearsalSchedule());
        assertEquals(org.getCreatedAt(), dto.getCreatedAt());

        assertEquals(1, dto.getAdminUsernames().size());
        assertEquals("John Doe", dto.getAdminUsernames().get(0));

        assertEquals(1, dto.getConcerts().size());
        assertEquals("Winter Concert", dto.getConcerts().get(0).getTitle());

        assertEquals(1, dto.getAnnualNeeds().size());
        assertEquals("Oboe", dto.getAnnualNeeds().get(0).getInstrument());

        assertEquals(1, dto.getRehearsalManuals().size());
        assertEquals("Rehearsal Room 1", dto.getRehearsalManuals().get(0).getLocation());

        assertEquals(1, dto.getRehearsalSchedules().size());
        assertEquals("Rehearsal Room 2", dto.getRehearsalSchedules().get(0).getLocation());
        assertEquals("Philharmonie", dto.getRehearsalSchedules().get(0).getOrganizationName());
    }

    @Test
    void testToDtoList() {
        Organization org1 = Organization.builder().id(1L).name("Org 1").build();
        Organization org2 = Organization.builder().id(2L).name("Org 2").build();

        List<OrganizationDTO> dtos = OrganizationMapper.toDto(List.of(org1, org2));

        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        assertEquals("Org 1", dtos.get(0).getName());
        assertEquals("Org 2", dtos.get(1).getName());
    }

    @Test
    void testToStringAdminNull() {
        assertEquals("", OrganizationMapper.toStringAdmin((OrganizationAdmin) null));
        OrganizationAdmin adminNoUser = new OrganizationAdmin();
        assertEquals("", OrganizationMapper.toStringAdmin(adminNoUser));
    }

    @Test
    void testToStringAdminListNull() {
        assertTrue(OrganizationMapper.toStringAdmin((List<OrganizationAdmin>) null).isEmpty());
    }

    @Test
    void testToEntityNull() {
        assertNull(OrganizationMapper.toEntity((OrganizationDTO) null));
    }

    @Test
    void testToEntity() {
        OrganizationDTO dto = OrganizationDTO.builder()
                .id(1L)
                .name("Philharmonie")
                .city("Paris")
                .description("Description")
                .concertDress("Dress")
                .logisticsInfo("Info")
                .formationType("Symphony")
                .musicianCount(10)
                .rehearsalAddress("Address")
                .rehearsalSchedule("Schedule")
                .createdAt(Instant.now())
                .concerts(List.of(ConcertDTO.builder().id(10L).title("Concert").build()))
                .annualNeeds(List.of(AnnualNeedDTO.builder().id(20L).instrument("Violin").build()))
                .rehearsalManuals(List.of(RehearsalManualDTO.builder().id(30L).location("Room A").build()))
                .rehearsalSchedules(List.of(RehearsalScheduleDTO.builder().id(40L).location("Room B").build()))
                .build();

        Organization entity = OrganizationMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getCity(), entity.getCity());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getConcertDress(), entity.getConcertDress());
        assertEquals(dto.getLogisticsInfo(), entity.getLogisticsInfo());
        assertEquals(dto.getFormationType(), entity.getFormationType());
        assertEquals(dto.getMusicianCount(), entity.getMusicianCount());
        assertEquals(dto.getRehearsalAddress(), entity.getRehearsalAddress());
        assertEquals(dto.getRehearsalSchedule(), entity.getRehearsalSchedule());
        assertEquals(dto.getCreatedAt(), entity.getCreatedAt());

        assertEquals(1, entity.getConcerts().size());
        assertEquals(10L, entity.getConcerts().get(0).getId());

        assertEquals(1, entity.getAnnualNeeds().size());
        assertEquals(20L, entity.getAnnualNeeds().get(0).getId());

        assertEquals(1, entity.getRehearsalManuals().size());
        assertEquals(30L, entity.getRehearsalManuals().get(0).getId());

        assertEquals(1, entity.getRehearsalSchedules().size());
        assertEquals(40L, entity.getRehearsalSchedules().get(0).getId());
    }

    @Test
    void testConcertNull() {
        assertNull(OrganizationMapper.toEntity((ConcertDTO) null));
        assertNull(OrganizationMapper.toDto((Concert) null));
    }

    @Test
    void testAnnualNeedsNull() {
        assertNull(OrganizationMapper.toEntity((AnnualNeedDTO) null));
        assertNull(OrganizationMapper.toDto((AnnualNeeds) null));
    }

    @Test
    void testRehearsalManualNull() {
        assertNull(OrganizationMapper.toEntity((RehearsalManualDTO) null));
        assertNull(OrganizationMapper.toDto((RehearsalManual) null));
    }

    @Test
    void testRehearsalScheduleNull() {
        assertNull(OrganizationMapper.toEntity((RehearsalScheduleDTO) null));
        assertNull(OrganizationMapper.toDto((RehearsalSchedule) null));
    }
}
