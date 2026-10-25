package ashzuu.project.copupitreback.services.email;

/**
 * Interface définissant les opérations d'envoi de notifications par email.
 * <p>
 * But : Découpler la logique métier d'envoi de courriels (promotions d'administrateurs,
 * création d'organisations, alertes de renforts, statut de candidatures)
 * du fournisseur d'envoi réel qui sera implémenté ultérieurement.
 * </p>
 */
public interface EmailService {

    /**
     * Notifie un utilisateur qu'il a été promu administrateur d'une organisation.
     *
     * @param toEmail           adresse email du destinataire
     * @param organizationName  nom de l'organisation
     * @param addedByUsername   nom d'utilisateur de la personne ayant effectué la promotion
     */
    void sendAdminPromotedNotification(String toEmail, String organizationName, String addedByUsername);

    /**
     * Envoie une notification de confirmation suite à la création d'une nouvelle organisation.
     *
     * @param toEmail          adresse email du créateur
     * @param organizationName nom de l'organisation créée
     */
    void sendOrganizationCreatedNotification(String toEmail, String organizationName);

    /**
     * Alerte les musiciens abonnés d'une nouvelle annonce de renfort publiée par un ensemble.
     *
     * @param toEmail           adresse email du destinataire
     * @param organizationName  nom de l'organisation émettrice
     * @param announcementTitle titre de l'annonce publiée
     */
    void sendNewAnnouncementAlert(String toEmail, String organizationName, String announcementTitle);

    /**
     * Notifie un candidat de l'évolution du statut de sa candidature à une annonce de renfort.
     *
     * @param toEmail          adresse email du candidat
     * @param candidateName    nom ou pseudo du candidat
     * @param status           nouveau statut (ex: ACCEPTED, DECLINED)
     * @param organizationName nom de l'organisation
     */
    void sendApplicationStatusUpdate(String toEmail, String candidateName, String status, String organizationName);

    /**
     * Notifie l'évolution du statut d'une demande de prêt de matériel (RG-MAT-04).
     *
     * @param toEmail          adresse email du destinataire
     * @param organizationName nom de l'organisation émettrice
     * @param equipmentName    nom du matériel concerné
     * @param status           nouveau statut (OPEN, MET, CLOSED)
     */
    void sendEquipmentRequestStatusUpdate(String toEmail, String organizationName, String equipmentName, String status);
}
