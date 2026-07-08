package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
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
class ReinforcementRepositoryTest {

    @Autowired
    private ReinforcementRepository reinforcementRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void testSaveAndFind() {
        Organization org = Organization.builder()
                .name("Reinforcement Org")
                .city("Bordeaux")
                .createdAt(Instant.now())
                .build();
        org = organizationRepository.save(org);

        ReinforcementRequest request = ReinforcementRequest.builder()
                .title("Need Violin")
                .instrumentNeeded("Violin")
                .requiredQuantity(3)
                .status("OPEN")
                .organization(org)
                .createdAt(Instant.now())
                .build();

        ReinforcementRequest saved = reinforcementRepository.save(request);
        assertNotNull(saved.getId());

        Optional<ReinforcementRequest> found = reinforcementRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Violin", found.get().getInstrumentNeeded());
    }
}
