package ashzuu.project.copupitreback.mapper;

import ashzuu.project.copupitreback.dto.permanent.PermanentAnnouncementDTO;
import ashzuu.project.copupitreback.dto.reinforcement.ReinforcementResponseDTO;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeedApplication;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeeds;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PermanentMapperTest {

    @Test
    void testToDtoNull() {
        assertNull(PermanentMapper.toDto(null));
        assertTrue(PermanentMapper.toDtoList(null).isEmpty());
    }

    @Test
    void testToResponseDtoNull() {
        assertNull(PermanentMapper.toResponseDto(null));
        assertTrue(PermanentMapper.toResponseDtoList(null).isEmpty());
    }

    @Test
    void testToResponseDto() {
        Organization org = Organization.builder().id(7L).name("Philharmonie").city("Lyon").build();
        User admin = User.builder().username("phil_admin").email("admin@philharmonie.fr").build();
        OrganizationAdmin orgAdmin = new OrganizationAdmin();
        orgAdmin.setUser(admin);
        orgAdmin.setOrganization(org);
        org.setOrganizationAdmins(List.of(orgAdmin));

        AnnualNeeds need = AnnualNeeds.builder()
                .id(12L)
                .instrument("Flûte")
                .requiredCount(2)
                .organization(org)
                .build();

        User applicant = User.builder()
                .username("flutist_lucie")
                .firstName("Lucie")
                .lastName("Bernard")
                .email("lucie@example.com")
                .mainInstrument("Flûte traversière")
                .build();

        AnnualNeedApplication app = AnnualNeedApplication.builder()
                .id(99L)
                .annualNeed(need)
                .user(applicant)
                .status("PENDING")
                .appliedAt(Instant.now())
                .message("Très motivée !")
                .build();

        ReinforcementResponseDTO dto = PermanentMapper.toResponseDto(app);

        assertNotNull(dto);
        assertEquals(99L, dto.getId());
        assertEquals(12L, dto.getAnnouncementId());
        assertEquals("Flûte - Philharmonie", dto.getAnnouncementTitle());
        assertEquals("flutist_lucie", dto.getRespondentUsername());
        assertEquals("Lucie Bernard", dto.getRespondentFullName());
        assertEquals("Flûte traversière", dto.getRespondentInstrument());
        assertEquals("lucie@example.com", dto.getRespondentEmail());
        assertEquals("PENDING", dto.getStatus());
        assertEquals("Très motivée !", dto.getMessage());
        assertEquals(7L, dto.getOrganizationId());
        assertEquals("Philharmonie", dto.getOrganizationName());
        assertEquals("admin@philharmonie.fr", dto.getOrganizationEmail());
        assertEquals("PERMANENT", dto.getType());
        assertEquals("Lyon", dto.getEventLocation());
    }
}
