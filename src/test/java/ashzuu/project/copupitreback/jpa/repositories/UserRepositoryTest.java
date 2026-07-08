package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        User user = User.builder()
                .username("repouser")
                .email("repo@test.com")
                .firstName("Repo")
                .lastName("User")
                .passwordHash("hash")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();

        User saved = userRepository.save(user);
        assertNotNull(saved.getId());

        Optional<User> found = userRepository.findByUsername("repouser");
        assertTrue(found.isPresent());
        assertEquals("repo@test.com", found.get().getEmail());

        Optional<User> foundByEmail = userRepository.findByEmail("repo@test.com");
        assertTrue(foundByEmail.isPresent());

        Optional<User> foundByOr = userRepository.findByUsernameOrEmail("repouser", "other@test.com");
        assertTrue(foundByOr.isPresent());

        Optional<User> foundByOrEmail = userRepository.findByUsernameOrEmail("other", "repo@test.com");
        assertTrue(foundByOrEmail.isPresent());

        assertTrue(userRepository.existsByUsername("repouser"));
        assertTrue(userRepository.existsByEmail("repo@test.com"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
        assertFalse(userRepository.existsByEmail("nonexistent@test.com"));
    }
}
