package ashzuu.project.copupitreback.services.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implémentation factice (No-Op) du service de messagerie {@link EmailService}.
 * <p>
 * Rôle : Permet à Spring de s'initialiser correctement sans dépendance externe
 * tout en traçant les événements d'envoi dans les journaux applicatifs.
 * </p>
 */
@Service
public class NoOpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailService.class);

    @Override
    public void sendAdminPromotedNotification(String toEmail, String organizationName, String addedByUsername) {
        log.info("[NoOpEmailService] Promotion admin - To: {}, Org: {}, Par: {}", toEmail, organizationName, addedByUsername);
    }

    @Override
    public void sendOrganizationCreatedNotification(String toEmail, String organizationName) {
        log.info("[NoOpEmailService] Création organisation - To: {}, Org: {}", toEmail, organizationName);
    }

    @Override
    public void sendNewAnnouncementAlert(String toEmail, String organizationName, String announcementTitle) {
        log.info("[NoOpEmailService] Nouvelle annonce - To: {}, Org: {}, Titre: {}", toEmail, organizationName, announcementTitle);
    }

    @Override
    public void sendApplicationStatusUpdate(String toEmail, String candidateName, String status, String organizationName) {
        log.info("[NoOpEmailService] Statut candidature - To: {}, Candidat: {}, Statut: {}, Org: {}",
                toEmail, candidateName, status, organizationName);
    }
}
