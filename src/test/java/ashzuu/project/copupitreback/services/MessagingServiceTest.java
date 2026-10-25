package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.message.MessageDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.Message;
import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.*;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock
    private MessageRepository messageRepo;

    @Mock
    private ApplicationRepository applicationRepo;

    @Mock
    private AnnualNeedApplicationRepository annualNeedApplicationRepo;

    @Mock
    private EquipmentRepository equipmentRepo;

    @Mock
    private OrganizationRepository organizationRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MessagingService service;

    private User user;
    private UserDetailsImpl userDetails;
    private Organization organization;
    private ReinforcementRequest reinforcementRequest;
    private Application application;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("musician").firstName("Pierre").lastName("Durand").build();
        userDetails = new UserDetailsImpl(user);

        organization = Organization.builder().id(10L).name("Orchestre Philharmonique").build();
        reinforcementRequest = ReinforcementRequest.builder().id(20L).organization(organization).title("Hautbois solo").build();
        application = Application.builder().id(30L).request(reinforcementRequest).user(user).status("PENDING").build();
    }

    @Test
    void testGetMessagesSuccessForApplicant() {
        when(applicationRepo.findById(30L)).thenReturn(Optional.of(application));
        Message msg = Message.builder()
                .id(1L)
                .contextType("APPLICATION")
                .contextId(30L)
                .sender(user)
                .content("Bonjour, disponible pour les répétitions")
                .createdAt(Instant.now())
                .build();
        when(messageRepo.findByContextOrderByCreatedAtAsc("APPLICATION", 30L)).thenReturn(List.of(msg));

        List<MessageDTO> result = service.getMessages("APPLICATION", 30L, userDetails);

        assertEquals(1, result.size());
        assertEquals("Bonjour, disponible pour les répétitions", result.get(0).getContent());
        assertTrue(result.get(0).isMe());
    }

    @Test
    void testGetMessagesForbiddenForThirdParty() {
        User thirdParty = User.builder().id(99L).username("intruder").build();
        UserDetailsImpl thirdPartyDetails = new UserDetailsImpl(thirdParty);

        when(applicationRepo.findById(30L)).thenReturn(Optional.of(application));
        when(organizationRepo.isAdminOfOrganization(99L, 10L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getMessages("APPLICATION", 30L, thirdPartyDetails));
    }

    @Test
    void testSendMessageSuccess() {
        when(applicationRepo.findById(30L)).thenReturn(Optional.of(application));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User admin = User.builder().id(2L).username("admin_org").build();
        when(organizationRepo.findAdminsByOrganizationId(10L)).thenReturn(List.of(admin));

        Message saved = Message.builder()
                .id(100L)
                .contextType("APPLICATION")
                .contextId(30L)
                .sender(user)
                .content("Voici mes disponibilités")
                .createdAt(Instant.now())
                .build();
        when(messageRepo.save(any(Message.class))).thenReturn(saved);

        MessageDTO result = service.sendMessage("APPLICATION", 30L, "Voici mes disponibilités", userDetails);

        assertNotNull(result);
        assertEquals("Voici mes disponibilités", result.getContent());
        verify(messageRepo).save(any(Message.class));
        verify(notificationService).createNotification(
                eq(admin),
                eq("NEW_MESSAGE"),
                anyString(),
                anyString(),
                eq("/dashboard?contextType=APPLICATION&contextId=30"),
                eq("APPLICATION"),
                eq(30L)
        );
    }

    @Test
    void testSendMessageBlankContent() {
        assertThrows(IllegalArgumentException.class, () -> service.sendMessage("APPLICATION", 30L, "   ", userDetails));
    }

    @Test
    void testSendMessageEquipment() {
        EquipmentRequest eqReq = EquipmentRequest.builder().id(50L).equipmentName("Timpani").organization(organization).build();
        when(equipmentRepo.findById(50L)).thenReturn(Optional.of(eqReq));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        User admin = User.builder().id(2L).build();
        when(organizationRepo.findAdminsByOrganizationId(10L)).thenReturn(List.of(admin));

        Message saved = Message.builder()
                .id(101L)
                .contextType("EQUIPMENT")
                .contextId(50L)
                .sender(user)
                .content("Je peux prêter mes timbales")
                .createdAt(Instant.now())
                .build();
        when(messageRepo.save(any(Message.class))).thenReturn(saved);

        MessageDTO result = service.sendMessage("EQUIPMENT", 50L, "Je peux prêter mes timbales", userDetails);

        assertNotNull(result);
        assertEquals("Je peux prêter mes timbales", result.getContent());
    }

    @Test
    void testGetUserConversations() {
        when(applicationRepo.findByUserId(1L)).thenReturn(List.of(application));
        when(annualNeedApplicationRepo.findByUserId(1L)).thenReturn(List.of());
        when(organizationRepo.getAllByUserId(1L)).thenReturn(List.of());
        when(messageRepo.findDistinctEquipmentContextIdsForUser(1L)).thenReturn(List.of());
        when(messageRepo.findFirstByContextTypeAndContextIdOrderByCreatedAtDesc("APPLICATION", 30L)).thenReturn(Optional.empty());
        when(messageRepo.countByContextTypeAndContextId("APPLICATION", 30L)).thenReturn(0L);

        List<ashzuu.project.copupitreback.dto.message.ConversationDTO> convs = service.getUserConversations(userDetails);

        assertNotNull(convs);
        assertEquals(1, convs.size());
        assertEquals("APPLICATION_30", convs.get(0).getId());
        assertEquals("Orchestre Philharmonique", convs.get(0).getOtherPartyName());
    }
}
