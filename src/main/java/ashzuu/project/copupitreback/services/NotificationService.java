package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.notification.NotificationDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.Notification;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.NotificationRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Notification createNotification(User recipient, String type, String title, String content, String link) {
        return createNotification(recipient, type, title, content, link, null, null);
    }

    @Transactional
    public Notification createNotification(User recipient, String type, String title, String content, String link, String contextType, Long contextId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .content(content)
                .link(link)
                .contextType(contextType)
                .contextId(contextId)
                .isRead(false)
                .createdAt(Instant.now())
                .build();
        return repo.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        return repo.findByUserIdOrderByCreatedAtDesc(userDetails.getId())
                .stream()
                .map(n -> NotificationDTO.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .link(n.getLink())
                        .contextType(n.getContextType())
                        .contextId(n.getContextId())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }

    public long getUnreadCount(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return 0;
        }
        return repo.countByUserIdAndIsReadFalse(userDetails.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        Notification notification = repo.findById(notificationId)
                .orElseThrow(() -> new UnknownID("Notification introuvable : " + notificationId));

        if (!notification.getUser().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("Cette notification ne vous appartient pas.");
        }

        notification.setRead(true);
        repo.save(notification);
    }

    @Transactional
    public void markAllAsRead(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        repo.markAllAsReadByUserId(userDetails.getId());
    }
}
