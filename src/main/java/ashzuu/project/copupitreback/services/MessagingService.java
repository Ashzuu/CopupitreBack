package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.message.ConversationDTO;
import ashzuu.project.copupitreback.dto.message.MessageDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.AnnualNeedApplication;
import ashzuu.project.copupitreback.jpa.entities.Application;
import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import ashzuu.project.copupitreback.jpa.entities.Message;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.*;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service gérant la messagerie interne contextuelle (US-COM-03 & US-COM-04).
 * Respecte strictement RG-COM-01 (aucune fuite d'email ou numéro personnel)
 * et RG-COM-02 (rattachement contextuel au renfort ou matériel).
 */
@Service
public class MessagingService {

    private final MessageRepository messageRepo;
    private final ApplicationRepository applicationRepo;
    private final AnnualNeedApplicationRepository annualNeedApplicationRepo;
    private final EquipmentRepository equipmentRepo;
    private final OrganizationRepository organizationRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public MessagingService(
            MessageRepository messageRepo,
            ApplicationRepository applicationRepo,
            AnnualNeedApplicationRepository annualNeedApplicationRepo,
            EquipmentRepository equipmentRepo,
            OrganizationRepository organizationRepo,
            UserRepository userRepo,
            NotificationService notificationService) {
        this.messageRepo = messageRepo;
        this.applicationRepo = applicationRepo;
        this.annualNeedApplicationRepo = annualNeedApplicationRepo;
        this.equipmentRepo = equipmentRepo;
        this.organizationRepo = organizationRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    public List<MessageDTO> getMessages(String contextType, Long contextId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        checkAccess(contextType, contextId, userDetails);

        List<Message> messages = messageRepo.findByContextOrderByCreatedAtAsc(contextType.toUpperCase(), contextId);
        return messages.stream()
                .map(m -> toDto(m, userDetails))
                .toList();
    }

    @Transactional
    public MessageDTO sendMessage(String contextType, Long contextId, String content, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du message ne peut pas être vide");
        }

        checkAccess(contextType, contextId, userDetails);

        User sender = userRepo.findById(userDetails.getId())
                .orElseThrow(() -> new UnknownID("Utilisateur introuvable : " + userDetails.getId()));

        Message message = Message.builder()
                .contextType(contextType.toUpperCase())
                .contextId(contextId)
                .sender(sender)
                .content(content.trim())
                .createdAt(Instant.now())
                .build();

        Message saved = messageRepo.save(message);

        // Notifier la contrepartie (US-COM-02)
        notifyOtherParty(contextType.toUpperCase(), contextId, sender, content.trim());

        return toDto(saved, userDetails);
    }

    /**
     * Récupère la liste de toutes les conversations actives pour l'utilisateur connecté.
     * Centralise les candidatures de renforts, de recrutements permanents et les prêts de matériel.
     */
    public List<ConversationDTO> getUserConversations(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        Long userId = userDetails.getId();
        Map<String, ConversationDTO> conversationMap = new LinkedHashMap<>();

        // 1. Candidatures déposées par l'utilisateur (renforts)
        List<Application> myApps = applicationRepo.findByUserId(userId);
        for (Application app : myApps) {
            String convId = "APPLICATION_" + app.getId();
            String orgName = app.getRequest() != null && app.getRequest().getOrganization() != null
                    ? app.getRequest().getOrganization().getName()
                    : "Organisation";
            String title = app.getRequest() != null ? app.getRequest().getTitle() : "Candidature de renfort";
            String subtitle = app.getRequest() != null ? "Renfort : " + app.getRequest().getInstrumentNeeded() : "";

            Optional<Message> latestMsg = messageRepo.findFirstByContextTypeAndContextIdOrderByCreatedAtDesc("APPLICATION", app.getId());
            long count = messageRepo.countByContextTypeAndContextId("APPLICATION", app.getId());

            String lastMsgText = latestMsg.map(Message::getContent).orElse(app.getMessage() != null ? app.getMessage() : "Candidature envoyée");
            Instant lastMsgDate = latestMsg.map(Message::getCreatedAt).orElse(app.getAppliedAt());

            conversationMap.put(convId, ConversationDTO.builder()
                    .id(convId)
                    .contextType("APPLICATION")
                    .contextId(app.getId())
                    .title(title)
                    .subtitle(subtitle)
                    .otherPartyName(orgName)
                    .otherPartyRole("Organisation")
                    .lastMessage(lastMsgText)
                    .lastMessageAt(lastMsgDate)
                    .messageCount(count)
                    .status(app.getStatus())
                    .build());
        }

        // 2. Candidatures déposées par l'utilisateur (besoins permanents)
        List<AnnualNeedApplication> myAnnualApps = annualNeedApplicationRepo.findByUserId(userId);
        for (AnnualNeedApplication app : myAnnualApps) {
            String convId = "ANNUAL_APPLICATION_" + app.getId();
            String orgName = app.getAnnualNeed() != null && app.getAnnualNeed().getOrganization() != null
                    ? app.getAnnualNeed().getOrganization().getName()
                    : "Organisation";
            String title = app.getAnnualNeed() != null ? "Poste permanent : " + app.getAnnualNeed().getInstrument() : "Recrutement permanent";
            String subtitle = orgName;

            Optional<Message> latestMsg = messageRepo.findFirstByContextTypeAndContextIdOrderByCreatedAtDesc("ANNUAL_APPLICATION", app.getId());
            long count = messageRepo.countByContextTypeAndContextId("ANNUAL_APPLICATION", app.getId());

            String lastMsgText = latestMsg.map(Message::getContent).orElse(app.getMessage() != null ? app.getMessage() : "Candidature envoyée");
            Instant lastMsgDate = latestMsg.map(Message::getCreatedAt).orElse(app.getAppliedAt());

            conversationMap.put(convId, ConversationDTO.builder()
                    .id(convId)
                    .contextType("ANNUAL_APPLICATION")
                    .contextId(app.getId())
                    .title(title)
                    .subtitle(subtitle)
                    .otherPartyName(orgName)
                    .otherPartyRole("Organisation")
                    .lastMessage(lastMsgText)
                    .lastMessageAt(lastMsgDate)
                    .messageCount(count)
                    .status(app.getStatus())
                    .build());
        }

        // 3. Candidatures reçues pour les organisations administrées par l'utilisateur
        List<Organization> adminOrgs = organizationRepo.getAllByUserId(userId);
        if (!adminOrgs.isEmpty()) {
            List<Long> orgIds = adminOrgs.stream().map(Organization::getId).toList();

            List<Application> receivedApps = applicationRepo.findByOrganizationIds(orgIds);
            for (Application app : receivedApps) {
                String convId = "APPLICATION_" + app.getId();
                if (!conversationMap.containsKey(convId)) {
                    String candidateName = (app.getUser() != null)
                            ? ((app.getUser().getFirstName() != null ? app.getUser().getFirstName() : "") + " "
                               + (app.getUser().getLastName() != null ? app.getUser().getLastName() : "")).trim()
                            : "Candidat";
                    if (candidateName.isEmpty() && app.getUser() != null) candidateName = app.getUser().getUsername();

                    String title = app.getRequest() != null ? "Candidature : " + app.getRequest().getTitle() : "Candidature";
                    String subtitle = app.getRequest() != null && app.getRequest().getOrganization() != null
                            ? app.getRequest().getOrganization().getName()
                            : "";

                    Optional<Message> latestMsg = messageRepo.findFirstByContextTypeAndContextIdOrderByCreatedAtDesc("APPLICATION", app.getId());
                    long count = messageRepo.countByContextTypeAndContextId("APPLICATION", app.getId());

                    String lastMsgText = latestMsg.map(Message::getContent).orElse(app.getMessage() != null ? app.getMessage() : "Nouvelle candidature reçue");
                    Instant lastMsgDate = latestMsg.map(Message::getCreatedAt).orElse(app.getAppliedAt());

                    conversationMap.put(convId, ConversationDTO.builder()
                            .id(convId)
                            .contextType("APPLICATION")
                            .contextId(app.getId())
                            .title(title)
                            .subtitle(subtitle)
                            .otherPartyName(candidateName)
                            .otherPartyRole("Candidat")
                            .lastMessage(lastMsgText)
                            .lastMessageAt(lastMsgDate)
                            .messageCount(count)
                            .status(app.getStatus())
                            .build());
                }
            }

            List<AnnualNeedApplication> receivedAnnualApps = annualNeedApplicationRepo.findByOrganizationIds(orgIds);
            for (AnnualNeedApplication app : receivedAnnualApps) {
                String convId = "ANNUAL_APPLICATION_" + app.getId();
                if (!conversationMap.containsKey(convId)) {
                    String candidateName = (app.getUser() != null)
                            ? ((app.getUser().getFirstName() != null ? app.getUser().getFirstName() : "") + " "
                               + (app.getUser().getLastName() != null ? app.getUser().getLastName() : "")).trim()
                            : "Candidat";
                    if (candidateName.isEmpty() && app.getUser() != null) candidateName = app.getUser().getUsername();

                    String title = app.getAnnualNeed() != null ? "Candidature permanente : " + app.getAnnualNeed().getInstrument() : "Recrutement";
                    String subtitle = app.getAnnualNeed() != null && app.getAnnualNeed().getOrganization() != null
                            ? app.getAnnualNeed().getOrganization().getName()
                            : "";

                    Optional<Message> latestMsg = messageRepo.findFirstByContextTypeAndContextIdOrderByCreatedAtDesc("ANNUAL_APPLICATION", app.getId());
                    long count = messageRepo.countByContextTypeAndContextId("ANNUAL_APPLICATION", app.getId());

                    String lastMsgText = latestMsg.map(Message::getContent).orElse(app.getMessage() != null ? app.getMessage() : "Nouvelle candidature reçue");
                    Instant lastMsgDate = latestMsg.map(Message::getCreatedAt).orElse(app.getAppliedAt());

                    conversationMap.put(convId, ConversationDTO.builder()
                            .id(convId)
                            .contextType("ANNUAL_APPLICATION")
                            .contextId(app.getId())
                            .title(title)
                            .subtitle(subtitle)
                            .otherPartyName(candidateName)
                            .otherPartyRole("Candidat")
                            .lastMessage(lastMsgText)
                            .lastMessageAt(lastMsgDate)
                            .messageCount(count)
                            .status(app.getStatus())
                            .build());
                }
            }
        }

        // 4. Prêts de matériel (EQUIPMENT) où l'utilisateur est intervenu ou admin de l'organisation émettrice
        List<Long> equipmentReqIds = messageRepo.findDistinctEquipmentContextIdsForUser(userId);
        for (Long reqId : equipmentReqIds) {
            String convId = "EQUIPMENT_" + reqId;
            if (!conversationMap.containsKey(convId)) {
                equipmentRepo.findById(reqId).ifPresent(req -> {
                    String orgName = req.getOrganization() != null ? req.getOrganization().getName() : "Organisation";
                    boolean isSenderAdmin = req.getOrganization() != null && organizationRepo.isAdminOfOrganization(userId, req.getOrganization().getId());

                    Optional<Message> latestMsg = messageRepo.findFirstByContextTypeAndContextIdOrderByCreatedAtDesc("EQUIPMENT", req.getId());
                    long count = messageRepo.countByContextTypeAndContextId("EQUIPMENT", req.getId());

                    conversationMap.put(convId, ConversationDTO.builder()
                            .id(convId)
                            .contextType("EQUIPMENT")
                            .contextId(req.getId())
                            .title("Prêt : " + req.getEquipmentName())
                            .subtitle("Organisation : " + orgName)
                            .otherPartyName(orgName)
                            .otherPartyRole(isSenderAdmin ? "Demandeur" : "Organisation")
                            .lastMessage(latestMsg.map(Message::getContent).orElse("Proposition de prêt"))
                            .lastMessageAt(latestMsg.map(Message::getCreatedAt).orElse(req.getCreatedAt()))
                            .messageCount(count)
                            .status(req.getStatus() != null ? req.getStatus() : "OPEN")
                            .build());
                });
            }
        }

        // Tri par date du dernier message descendante (le plus récent en premier)
        return conversationMap.values().stream()
                .sorted((a, b) -> {
                    if (a.getLastMessageAt() == null && b.getLastMessageAt() == null) return 0;
                    if (a.getLastMessageAt() == null) return 1;
                    if (b.getLastMessageAt() == null) return -1;
                    return b.getLastMessageAt().compareTo(a.getLastMessageAt());
                })
                .toList();
    }

    private void checkAccess(String contextType, Long contextId, UserDetailsImpl userDetails) {
        String normalizedType = contextType != null ? contextType.trim().toUpperCase() : "";

        if ("APPLICATION".equals(normalizedType)) {
            Application app = applicationRepo.findById(contextId)
                    .orElseThrow(() -> new UnknownID("Candidature introuvable : " + contextId));

            boolean isApplicant = app.getUser() != null && app.getUser().getId().equals(userDetails.getId());
            boolean isAdmin = app.getRequest() != null && app.getRequest().getOrganization() != null
                    && organizationRepo.isAdminOfOrganization(userDetails.getId(), app.getRequest().getOrganization().getId());

            if (!isApplicant && !isAdmin) {
                throw new AccessDeniedException("Vous n'avez pas accès à cette conversation.");
            }
        } else if ("ANNUAL_APPLICATION".equals(normalizedType) || "ANNUAL_NEED_APPLICATION".equals(normalizedType)) {
            AnnualNeedApplication app = annualNeedApplicationRepo.findById(contextId)
                    .orElseThrow(() -> new UnknownID("Candidature de musicien permanent introuvable : " + contextId));

            boolean isApplicant = app.getUser() != null && app.getUser().getId().equals(userDetails.getId());
            boolean isAdmin = app.getAnnualNeed() != null && app.getAnnualNeed().getOrganization() != null
                    && organizationRepo.isAdminOfOrganization(userDetails.getId(), app.getAnnualNeed().getOrganization().getId());

            if (!isApplicant && !isAdmin) {
                throw new AccessDeniedException("Vous n'avez pas accès à cette conversation.");
            }
        } else if ("EQUIPMENT".equals(normalizedType)) {
            equipmentRepo.findById(contextId)
                    .orElseThrow(() -> new UnknownID("Demande de matériel introuvable : " + contextId));
        } else {
            throw new IllegalArgumentException("Type de contexte invalide : " + contextType);
        }
    }

    private void notifyOtherParty(String contextType, Long contextId, User sender, String content) {
        String excerpt = content.length() > 80 ? content.substring(0, 77) + "..." : content;
        String rawName = (sender.getFirstName() != null ? sender.getFirstName() : "") + " " + (sender.getLastName() != null ? sender.getLastName() : "");
        String senderName = rawName.trim().isEmpty() ? sender.getUsername() : rawName.trim();

        if ("APPLICATION".equalsIgnoreCase(contextType)) {
            applicationRepo.findById(contextId).ifPresent(app -> {
                boolean isApplicant = app.getUser() != null && app.getUser().getId().equals(sender.getId());
                if (isApplicant) {
                    if (app.getRequest() != null && app.getRequest().getOrganization() != null) {
                        Long orgId = app.getRequest().getOrganization().getId();
                        List<User> admins = organizationRepo.findAdminsByOrganizationId(orgId);
                        for (User admin : admins) {
                            notificationService.createNotification(
                                    admin,
                                    "NEW_MESSAGE",
                                    "Nouveau message de " + senderName,
                                    excerpt,
                                    "/dashboard?contextType=APPLICATION&contextId=" + contextId,
                                    "APPLICATION",
                                    contextId
                            );
                        }
                    }
                } else {
                    if (app.getUser() != null) {
                        notificationService.createNotification(
                                app.getUser(),
                                "NEW_MESSAGE",
                                "Nouveau message concernant votre candidature",
                                excerpt,
                                "/mes-candidatures?contextType=APPLICATION&contextId=" + contextId,
                                "APPLICATION",
                                contextId
                        );
                    }
                }
            });
        } else if ("ANNUAL_APPLICATION".equalsIgnoreCase(contextType) || "ANNUAL_NEED_APPLICATION".equalsIgnoreCase(contextType)) {
            annualNeedApplicationRepo.findById(contextId).ifPresent(app -> {
                boolean isApplicant = app.getUser() != null && app.getUser().getId().equals(sender.getId());
                if (isApplicant) {
                    if (app.getAnnualNeed() != null && app.getAnnualNeed().getOrganization() != null) {
                        Long orgId = app.getAnnualNeed().getOrganization().getId();
                        List<User> admins = organizationRepo.findAdminsByOrganizationId(orgId);
                        for (User admin : admins) {
                            notificationService.createNotification(
                                    admin,
                                    "NEW_MESSAGE",
                                    "Nouveau message de " + senderName,
                                    excerpt,
                                    "/dashboard?contextType=ANNUAL_APPLICATION&contextId=" + contextId,
                                    "ANNUAL_APPLICATION",
                                    contextId
                            );
                        }
                    }
                } else {
                    if (app.getUser() != null) {
                        notificationService.createNotification(
                                app.getUser(),
                                "NEW_MESSAGE",
                                "Nouveau message concernant votre candidature permanente",
                                excerpt,
                                "/mes-candidatures?contextType=ANNUAL_APPLICATION&contextId=" + contextId,
                                "ANNUAL_APPLICATION",
                                contextId
                        );
                    }
                }
            });
        } else if ("EQUIPMENT".equalsIgnoreCase(contextType)) {
            equipmentRepo.findById(contextId).ifPresent(req -> {
                if (req.getOrganization() != null) {
                    boolean isSenderAdmin = organizationRepo.isAdminOfOrganization(sender.getId(), req.getOrganization().getId());
                    if (!isSenderAdmin) {
                        List<User> admins = organizationRepo.findAdminsByOrganizationId(req.getOrganization().getId());
                        for (User admin : admins) {
                            notificationService.createNotification(
                                    admin,
                                    "NEW_MESSAGE",
                                    "Message pour le prêt de matériel (" + req.getEquipmentName() + ")",
                                    excerpt,
                                    "/materiels?contextType=EQUIPMENT&contextId=" + contextId,
                                    "EQUIPMENT",
                                    contextId
                            );
                        }
                    }
                }
            });
        }
    }

    private MessageDTO toDto(Message message, UserDetailsImpl userDetails) {
        User sender = message.getSender();
        boolean isMe = sender != null && sender.getId().equals(userDetails.getId());
        String senderRole = "USER";

        if ("APPLICATION".equalsIgnoreCase(message.getContextType())) {
            var appOpt = applicationRepo.findById(message.getContextId());
            if (appOpt.isPresent() && appOpt.get().getRequest() != null && appOpt.get().getRequest().getOrganization() != null) {
                boolean isAdmin = organizationRepo.isAdminOfOrganization(sender.getId(), appOpt.get().getRequest().getOrganization().getId());
                senderRole = isAdmin ? "ORGANIZATION_ADMIN" : "MUSICIAN";
            }
        } else if ("ANNUAL_APPLICATION".equalsIgnoreCase(message.getContextType()) || "ANNUAL_NEED_APPLICATION".equalsIgnoreCase(message.getContextType())) {
            var appOpt = annualNeedApplicationRepo.findById(message.getContextId());
            if (appOpt.isPresent() && appOpt.get().getAnnualNeed() != null && appOpt.get().getAnnualNeed().getOrganization() != null) {
                boolean isAdmin = organizationRepo.isAdminOfOrganization(sender.getId(), appOpt.get().getAnnualNeed().getOrganization().getId());
                senderRole = isAdmin ? "ORGANIZATION_ADMIN" : "MUSICIAN";
            }
        } else if ("EQUIPMENT".equalsIgnoreCase(message.getContextType())) {
            var eqOpt = equipmentRepo.findById(message.getContextId());
            if (eqOpt.isPresent() && eqOpt.get().getOrganization() != null) {
                boolean isAdmin = organizationRepo.isAdminOfOrganization(sender.getId(), eqOpt.get().getOrganization().getId());
                senderRole = isAdmin ? "ORGANIZATION_ADMIN" : "MUSICIAN";
            }
        }

        return MessageDTO.builder()
                .id(message.getId())
                .contextType(message.getContextType())
                .contextId(message.getContextId())
                .senderUsername(sender != null ? sender.getUsername() : "")
                .senderFullName(sender != null ? ((sender.getFirstName() != null ? sender.getFirstName() : "") + " " + (sender.getLastName() != null ? sender.getLastName() : "")).trim() : "")
                .senderRole(senderRole)
                .isMe(isMe)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
