package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.organization.*;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.*;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdmin;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationAdminId;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrganizationService organizationService;

    @Test
    void testGetAll() {
        Organization o = Organization.builder().id(1L).name("Org").build();
        when(organizationRepository.findAll()).thenReturn(List.of(o));

        List<OrganizationDTO> result = organizationService.getAll();
        assertEquals(1, result.size());
        assertEquals("Org", result.get(0).getName());
    }

    @Test
    void testGetByIdSuccess() {
        Organization o = Organization.builder().id(1L).name("Org").build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(o));

        OrganizationDTO result = organizationService.getById("1");
        assertNotNull(result);
        assertEquals("Org", result.getName());
    }

    @Test
    void testGetByIdNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> organizationService.getById("1"));
    }

    @Test
    void testGetOrganizationFromUser() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        Organization o = Organization.builder().id(1L).name("Org").build();
        when(organizationRepository.getAllByUserId(5L)).thenReturn(List.of(o));

        List<OrganizationDTO> result = organizationService.getOrganizationFromUser(details);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAnnouncementsOnOrganizationSuccess() {
        Organization o = Organization.builder().id(1L).reinforcementRequests(new ArrayList<>()).build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(o));

        var result = organizationService.getAnnouncementsOnOrganization("1");
        assertNotNull(result);
    }

    @Test
    void testGetAnnouncementsOnOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnknownID.class, () -> organizationService.getAnnouncementsOnOrganization("1"));
    }

    @Test
    void testGetEquipmentRequestOnOrganizationSuccess() {
        Organization o = Organization.builder().id(1L).equipmentRequests(new ArrayList<>()).build();
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(o));

        var result = organizationService.getEquipmentRequestOnOrganization("1");
        assertNotNull(result);
    }

    @Test
    void testGetEquipmentRequestOnOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UnknownID.class, () -> organizationService.getEquipmentRequestOnOrganization("1"));
    }

    @Test
    void testSaveCreateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).username("adminuser").email("admin@test.com").build());
        User adminUser = User.builder().id(5L).username("adminuser").email("admin@test.com").build();
        OrganizationDTO dto = OrganizationDTO.builder().name("New Org").build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(adminUser));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        OrganizationDTO result = organizationService.save(dto, details);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("New Org", result.getName());
        verify(organizationRepository).save(any(Organization.class));
        verify(emailService).sendOrganizationCreatedNotification("admin@test.com", "New Org");
    }

    @Test
    void testSaveCreateWithConcertsSchedulesAndAnnualNeeds() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).username("creator").email("creator@test.com").build());
        User adminUser = User.builder().id(5L).username("creator").email("creator@test.com").build();

        ConcertDTO concertDto = ConcertDTO.builder().title("Gala").eventDate(Instant.now()).location("Opéra").build();
        AnnualNeedDTO needDto = AnnualNeedDTO.builder().instrument("Trompette").count(2).description("1er pupitre").build();
        RehearsalScheduleDTO scheduleDto = RehearsalScheduleDTO.builder().dayOfWeek("MONDAY").startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(22, 0)).build();
        RehearsalManualDTO manualDto = RehearsalManualDTO.builder().manualDate(LocalDate.now().plusDays(10)).startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(18, 0)).build();

        OrganizationDTO dto = OrganizationDTO.builder()
                .name("Harmonie Municipale")
                .city("Lyon")
                .concerts(List.of(concertDto))
                .annualNeeds(List.of(needDto))
                .rehearsalSchedules(List.of(scheduleDto))
                .rehearsalManuals(List.of(manualDto))
                .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(adminUser));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization saved = inv.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        OrganizationDTO result = organizationService.save(dto, details);

        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals("Harmonie Municipale", result.getName());
        assertEquals(1, result.getConcerts().size());
        assertEquals("Gala", result.getConcerts().get(0).getTitle());
        assertEquals(1, result.getAnnualNeeds().size());
        assertEquals("Trompette", result.getAnnualNeeds().get(0).getInstrument());
        assertEquals(2, result.getAnnualNeeds().get(0).getRequiredCount());
        assertEquals(2, result.getAnnualNeeds().get(0).getCount());
        assertEquals(1, result.getRehearsalSchedules().size());
        assertEquals(1, result.getRehearsalManuals().size());
        verify(emailService).sendOrganizationCreatedNotification("creator@test.com", "Harmonie Municipale");
    }

    @Test
    void testSaveCreateAdminNotFound() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        OrganizationDTO dto = OrganizationDTO.builder().name("New Org").build();

        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> organizationService.save(dto, details));
    }

    @Test
    void testSaveUpdateSuccess() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        Organization org = Organization.builder()
                .id(1L)
                .name("Old Org")
                .concerts(new ArrayList<>())
                .annualNeeds(new ArrayList<>())
                .rehearsalManuals(new ArrayList<>())
                .rehearsalSchedules(new ArrayList<>())
                .build();
        OrganizationDTO dto = OrganizationDTO.builder()
                .id(1L)
                .name("Updated Org")
                .build();

        when(organizationRepository.isAdminOfOrganization(5L, 1L)).thenReturn(true);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationDTO result = organizationService.save(dto, details);

        assertNotNull(result);
        assertEquals("Updated Org", result.getName());
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void testSaveUpdateWithConcertsAndAnnualNeedsCascade() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());

        Concert existingConcert = Concert.builder().id(10L).title("Old Concert").build();
        AnnualNeeds existingNeed = AnnualNeeds.builder().id(20L).instrument("Violon").requiredCount(1).build();

        List<Concert> concerts = new ArrayList<>(List.of(existingConcert));
        List<AnnualNeeds> needs = new ArrayList<>(List.of(existingNeed));

        Organization org = Organization.builder()
                .id(1L)
                .name("Orchestre")
                .concerts(concerts)
                .annualNeeds(needs)
                .rehearsalManuals(new ArrayList<>())
                .rehearsalSchedules(new ArrayList<>())
                .build();

        // DTO updates existing concert title and existing need count, adds a new need
        ConcertDTO updateConcertDto = ConcertDTO.builder().id(10L).title("Concert Printemps").build();
        AnnualNeedDTO updateNeedDto = AnnualNeedDTO.builder().id(20L).instrument("Violon").count(3).build();
        AnnualNeedDTO newNeedDto = AnnualNeedDTO.builder().instrument("Flûte").count(1).build();

        OrganizationDTO dto = OrganizationDTO.builder()
                .id(1L)
                .name("Orchestre Sympa")
                .concerts(List.of(updateConcertDto))
                .annualNeeds(List.of(updateNeedDto, newNeedDto))
                .build();

        when(organizationRepository.isAdminOfOrganization(5L, 1L)).thenReturn(true);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationDTO result = organizationService.save(dto, details);

        assertNotNull(result);
        assertEquals("Orchestre Sympa", result.getName());
        assertEquals("Concert Printemps", result.getConcerts().get(0).getTitle());
        assertEquals(2, result.getAnnualNeeds().size());
        assertEquals(3, result.getAnnualNeeds().get(0).getRequiredCount());
        assertEquals("Flûte", result.getAnnualNeeds().get(1).getInstrument());
        assertEquals(1, result.getAnnualNeeds().get(1).getRequiredCount());
    }

    @Test
    void testSaveUpdateNotAdmin() {
        UserDetailsImpl details = new UserDetailsImpl(User.builder().id(5L).build());
        OrganizationDTO dto = OrganizationDTO.builder().id(1L).build();

        when(organizationRepository.isAdminOfOrganization(5L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> organizationService.save(dto, details));
    }

    @Test
    void testAddAdminSuccess() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        User existingAdmin = User.builder().id(1L).username("admincaller").build();
        User newAdminUser = User.builder().id(2L).username("promoteduser").email("promoted@test.com").build();

        Organization org = Organization.builder()
                .id(10L)
                .name("Philharmonie")
                .organizationAdmins(new ArrayList<>())
                .build();

        OrganizationAdmin admin1 = new OrganizationAdmin();
        OrganizationAdminId id1 = new OrganizationAdminId();
        id1.setUserId(1L);
        id1.setOrganizationId(10L);
        admin1.setId(id1);
        admin1.setUser(existingAdmin);
        admin1.setOrganization(org);
        org.getOrganizationAdmins().add(admin1);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("promoteduser")).thenReturn(Optional.of(newAdminUser));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationDTO result = organizationService.addAdmin(10L, "promoteduser", callerDetails);

        assertNotNull(result);
        assertEquals(2, result.getAdminUsernames().size());
        assertTrue(result.getAdminUsernames().contains("promoteduser"));
        assertTrue(result.getAdminUsernames().contains("admincaller"));

        verify(emailService).sendAdminPromotedNotification("promoted@test.com", "Philharmonie", "admincaller");
        verify(organizationRepository).save(org);
    }

    @Test
    void testAddAdminNotAdminCaller() {
        User caller = User.builder().id(1L).username("nonadmin").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> organizationService.addAdmin(10L, "target", callerDetails));
        verifyNoInteractions(emailService);
    }

    @Test
    void testAddAdminOrgNotFound() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> organizationService.addAdmin(10L, "target", callerDetails));
    }

    @Test
    void testAddAdminUserNotFound() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> organizationService.addAdmin(10L, "unknown", callerDetails));
    }

    @Test
    void testAddAdminAlreadyAdmin() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        User targetUser = User.builder().id(2L).username("alreadyadmin").build();

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();
        OrganizationAdmin admin = new OrganizationAdmin();
        admin.setUser(targetUser);
        admin.setOrganization(org);
        org.getOrganizationAdmins().add(admin);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("alreadyadmin")).thenReturn(Optional.of(targetUser));

        assertThrows(IllegalArgumentException.class, () -> organizationService.addAdmin(10L, "alreadyadmin", callerDetails));
        verifyNoInteractions(emailService);
    }

    @Test
    void testRemoveAdminSuccess() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        User user1 = User.builder().id(1L).username("admincaller").build();
        User user2 = User.builder().id(2L).username("admintoremove").build();

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();

        OrganizationAdmin admin1 = new OrganizationAdmin();
        admin1.setUser(user1);
        admin1.setOrganization(org);

        OrganizationAdmin admin2 = new OrganizationAdmin();
        admin2.setUser(user2);
        admin2.setOrganization(org);

        org.getOrganizationAdmins().add(admin1);
        org.getOrganizationAdmins().add(admin2);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("admintoremove")).thenReturn(Optional.of(user2));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizationDTO result = organizationService.removeAdmin(10L, "admintoremove", callerDetails);

        assertNotNull(result);
        assertEquals(1, result.getAdminUsernames().size());
        assertEquals("admincaller", result.getAdminUsernames().get(0));
        verify(organizationRepository).save(org);
    }

    @Test
    void testRemoveAdminSelfForbidden() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        User user1 = User.builder().id(1L).username("admincaller").build();
        User user2 = User.builder().id(2L).username("otheradmin").build();

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();
        OrganizationAdmin admin1 = new OrganizationAdmin();
        admin1.setUser(user1);
        admin1.setOrganization(org);
        OrganizationAdmin admin2 = new OrganizationAdmin();
        admin2.setUser(user2);
        admin2.setOrganization(org);
        org.getOrganizationAdmins().add(admin1);
        org.getOrganizationAdmins().add(admin2);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("admincaller")).thenReturn(Optional.of(user1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> organizationService.removeAdmin(10L, "admincaller", callerDetails));
        assertTrue(ex.getMessage().contains("retirer vous-même"));
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void testRemoveAdminLastAdminForbidden() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        User user2 = User.builder().id(2L).username("otheradmin").build();

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();
        OrganizationAdmin admin2 = new OrganizationAdmin();
        admin2.setUser(user2);
        admin2.setOrganization(org);
        org.getOrganizationAdmins().add(admin2);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("otheradmin")).thenReturn(Optional.of(user2));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> organizationService.removeAdmin(10L, "otheradmin", callerDetails));
        assertTrue(ex.getMessage().contains("dernier administrateur"));
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void testRemoveAdminNotAdminCaller() {
        User caller = User.builder().id(1L).username("nonadmin").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> organizationService.removeAdmin(10L, "target", callerDetails));
    }

    @Test
    void testRemoveAdminTargetUserNotFound() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();
        org.getOrganizationAdmins().add(new OrganizationAdmin());
        org.getOrganizationAdmins().add(new OrganizationAdmin());

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

        assertThrows(UnknownID.class,
                () -> organizationService.removeAdmin(10L, "ghost", callerDetails));
    }

    @Test
    void testRemoveAdminTargetNotAdminOfOrg() {
        User caller = User.builder().id(1L).username("admincaller").build();
        UserDetailsImpl callerDetails = new UserDetailsImpl(caller);

        User user1 = User.builder().id(1L).username("admincaller").build();
        User user2 = User.builder().id(2L).username("adminother").build();
        User user3 = User.builder().id(3L).username("notanadmin").build();

        Organization org = Organization.builder().id(10L).name("Philharmonie").organizationAdmins(new ArrayList<>()).build();
        OrganizationAdmin admin1 = new OrganizationAdmin();
        admin1.setUser(user1);
        OrganizationAdmin admin2 = new OrganizationAdmin();
        admin2.setUser(user2);
        org.getOrganizationAdmins().add(admin1);
        org.getOrganizationAdmins().add(admin2);

        when(organizationRepository.isAdminOfOrganization(1L, 10L)).thenReturn(true);
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(org));
        when(userRepository.findByUsername("notanadmin")).thenReturn(Optional.of(user3));

        assertThrows(UnknownID.class,
                () -> organizationService.removeAdmin(10L, "notanadmin", callerDetails));
    }
}
