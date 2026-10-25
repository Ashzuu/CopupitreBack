package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.notification.NotificationDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.Notification;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.NotificationRepository;
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
class NotificationServiceTest {

    @Mock
    private NotificationRepository repo;

    @InjectMocks
    private NotificationService service;

    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void testCreateNotification() {
        Notification notif = Notification.builder()
                .id(100L)
                .user(user)
                .type("NEW_MESSAGE")
                .title("Title")
                .content("Content")
                .link("/dashboard")
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        when(repo.save(any(Notification.class))).thenReturn(notif);

        Notification result = service.createNotification(user, "NEW_MESSAGE", "Title", "Content", "/dashboard");

        assertNotNull(result);
        assertEquals("NEW_MESSAGE", result.getType());
        assertEquals("Title", result.getTitle());
        verify(repo).save(any(Notification.class));
    }

    @Test
    void testCreateNotificationWithContext() {
        Notification notif = Notification.builder()
                .id(101L)
                .user(user)
                .type("NEW_MESSAGE")
                .title("Title")
                .content("Content")
                .link("/dashboard")
                .contextType("APPLICATION")
                .contextId(42L)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        when(repo.save(any(Notification.class))).thenReturn(notif);

        Notification result = service.createNotification(user, "NEW_MESSAGE", "Title", "Content", "/dashboard", "APPLICATION", 42L);

        assertNotNull(result);
        assertEquals("APPLICATION", result.getContextType());
        assertEquals(42L, result.getContextId());
        verify(repo).save(any(Notification.class));
    }

    @Test
    void testGetUserNotifications() {
        Notification notif = Notification.builder()
                .id(100L)
                .user(user)
                .type("NEW_APPLICATION")
                .title("Nouvelle candidature")
                .content("Jean a postulé")
                .link("/dashboard")
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        when(repo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notif));

        List<NotificationDTO> result = service.getUserNotifications(userDetails);

        assertEquals(1, result.size());
        assertEquals("Nouvelle candidature", result.get(0).getTitle());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void testGetUserNotificationsUnauthenticated() {
        assertThrows(AccessDeniedException.class, () -> service.getUserNotifications(null));
    }

    @Test
    void testGetUnreadCount() {
        when(repo.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = service.getUnreadCount(userDetails);

        assertEquals(3L, count);
    }

    @Test
    void testGetUnreadCountNullDetails() {
        assertEquals(0L, service.getUnreadCount(null));
    }

    @Test
    void testMarkAsReadSuccess() {
        Notification notif = Notification.builder()
                .id(100L)
                .user(user)
                .isRead(false)
                .build();

        when(repo.findById(100L)).thenReturn(Optional.of(notif));

        service.markAsRead(100L, userDetails);

        assertTrue(notif.isRead());
        verify(repo).save(notif);
    }

    @Test
    void testMarkAsReadForbiddenOtherUser() {
        User other = User.builder().id(2L).build();
        Notification notif = Notification.builder()
                .id(100L)
                .user(other)
                .build();

        when(repo.findById(100L)).thenReturn(Optional.of(notif));

        assertThrows(AccessDeniedException.class, () -> service.markAsRead(100L, userDetails));
    }

    @Test
    void testMarkAsReadNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UnknownID.class, () -> service.markAsRead(99L, userDetails));
    }

    @Test
    void testMarkAllAsRead() {
        service.markAllAsRead(userDetails);
        verify(repo).markAllAsReadByUserId(1L);
    }
}
