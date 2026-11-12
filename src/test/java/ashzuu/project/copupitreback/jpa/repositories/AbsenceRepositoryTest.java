package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.organization.*;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AbsenceRepositoryTest {

    @Autowired
    private AbsenceRepository absenceRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private MusicianRepository musicianRepository;

    @Autowired
    private UserRepository userRepository;

    private Organization org;
    private Musician musician;
    private Absence absence1;
    private Absence absence2;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("musician_test")
                .email("musician_test@copupitre.fr")
                .firstName("Claire")
                .lastName("Fontaine")
                .passwordHash("hash")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        user = userRepository.save(user);

        org = Organization.builder()
                .name("Test Orchestra Absences")
                .city("Lyon")
                .createdAt(Instant.now())
                .build();
        org = organizationRepository.save(org);

        musician = Musician.builder()
                .firstName("Claire")
                .lastName("Fontaine")
                .email("musician_test@copupitre.fr")
                .instrument("Flûte")
                .user(user)
                .organizations(new HashSet<>(Collections.singletonList(org)))
                .build();
        musician = musicianRepository.save(musician);

        absence1 = Absence.builder()
                .organization(org)
                .musician(musician)
                .eventType(EventType.REHEARSAL)
                .eventDate(LocalDate.of(2026, 11, 15))
                .reason("Raison médicale")
                .status(AbsenceStatus.JUSTIFIED)
                .submittedByAdmin(false)
                .build();
        absence1 = absenceRepository.save(absence1);

        absence2 = Absence.builder()
                .organization(org)
                .musician(musician)
                .eventType(EventType.CONCERT)
                .eventDate(LocalDate.of(2026, 11, 20))
                .reason("Voyage professionnel")
                .status(AbsenceStatus.UNJUSTIFIED)
                .submittedByAdmin(true)
                .build();
        absence2 = absenceRepository.save(absence2);
    }

    @Test
    void findByFilters_withNullFilters_returnsAllOrganizationAbsences() {
        List<Absence> results = absenceRepository.findByFilters(
                org.getId(), null, null, null, null, null
        );
        assertEquals(2, results.size());
    }

    @Test
    void findByFilters_withSpecificFilters_filtersCorrectly() {
        List<Absence> rehearsalResults = absenceRepository.findByFilters(
                org.getId(), null, null, EventType.REHEARSAL, null, null
        );
        assertEquals(1, rehearsalResults.size());
        assertEquals(EventType.REHEARSAL, rehearsalResults.get(0).getEventType());

        List<Absence> dateResults = absenceRepository.findByFilters(
                org.getId(), LocalDate.of(2026, 11, 18), null, null, null, null
        );
        assertEquals(1, dateResults.size());
        assertEquals(LocalDate.of(2026, 11, 20), dateResults.get(0).getEventDate());

        List<Absence> statusResults = absenceRepository.findByFilters(
                org.getId(), null, null, null, AbsenceStatus.JUSTIFIED, null
        );
        assertEquals(1, statusResults.size());
        assertEquals(AbsenceStatus.JUSTIFIED, statusResults.get(0).getStatus());

        List<Absence> musicianResults = absenceRepository.findByFilters(
                org.getId(), null, null, null, null, musician.getId()
        );
        assertEquals(2, musicianResults.size());
    }

    @Test
    void findByOrganizationIdAndDateRange_withNullDates_returnsAll() {
        List<Absence> results = absenceRepository.findByOrganizationIdAndDateRange(org.getId(), null, null);
        assertEquals(2, results.size());
    }

    @Test
    void findByOrganizationIdAndDateRange_withDateBounds_returnsMatching() {
        List<Absence> results = absenceRepository.findByOrganizationIdAndDateRange(
                org.getId(),
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 16)
        );
        assertEquals(1, results.size());
        assertEquals(absence1.getId(), results.get(0).getId());
    }
}
