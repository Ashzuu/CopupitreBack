package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdminId;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCustomQueries() {
        User user = User.builder()
                .username("admin_user")
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .passwordHash("hash")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        user = userRepository.save(user);

        Organization org = Organization.builder()
                .name("Test Orchestra")
                .city("Paris")
                .createdAt(Instant.now())
                .build();
        org = organizationRepository.save(org);

        OrganizationAdmin admin = new OrganizationAdmin();
        OrganizationAdminId adminId = new OrganizationAdminId();
        adminId.setUserId(user.getId());
        adminId.setOrganizationId(org.getId());
        admin.setId(adminId);
        admin.setUser(user);
        admin.setOrganization(org);
        admin.setAddedAt(Instant.now());

        org.getOrganizationAdmins().add(admin);
        organizationRepository.save(org);

        List<Organization> orgs = organizationRepository.getAllByUserId(user.getId());
        assertEquals(1, orgs.size());
        assertEquals("Test Orchestra", orgs.get(0).getName());

        assertTrue(organizationRepository.isAdminOfOrganization(user.getId(), org.getId()));
        assertFalse(organizationRepository.isAdminOfOrganization(user.getId() + 999, org.getId()));
        assertFalse(organizationRepository.isAdminOfOrganization(user.getId(), org.getId() + 999));
    }
}
