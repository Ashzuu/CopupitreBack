package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EquipmentRepositoryTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void testSaveAndFind() {
        Organization org = Organization.builder()
                .name("Equipment Org")
                .city("Marseille")
                .createdAt(Instant.now())
                .build();
        org = organizationRepository.save(org);

        EquipmentRequest request = EquipmentRequest.builder()
                .title("Stands Request")
                .equipmentName("Music Stands")
                .duration("5 days")
                .description("Need stands")
                .status("OPEN")
                .organization(org)
                .createdAt(Instant.now())
                .build();

        EquipmentRequest saved = equipmentRepository.save(request);
        assertNotNull(saved.getId());

        Optional<EquipmentRequest> found = equipmentRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Music Stands", found.get().getEquipmentName());
    }
}
