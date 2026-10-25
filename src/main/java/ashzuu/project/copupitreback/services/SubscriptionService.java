package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.organization.SubscriptionStatusDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationSubscription;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationSubscriptionId;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationSubscriptionRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SubscriptionService {

    private final OrganizationSubscriptionRepository subscriptionRepo;
    private final OrganizationRepository organizationRepo;
    private final UserRepository userRepo;

    public SubscriptionService(
            OrganizationSubscriptionRepository subscriptionRepo,
            OrganizationRepository organizationRepo,
            UserRepository userRepo) {
        this.subscriptionRepo = subscriptionRepo;
        this.organizationRepo = organizationRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public SubscriptionStatusDTO subscribe(Long organizationId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        Organization org = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new UnknownID("Organisation inconnue : " + organizationId));

        User user = userRepo.findById(userDetails.getId())
                .orElseThrow(() -> new UnknownID("Utilisateur introuvable : " + userDetails.getId()));

        if (!subscriptionRepo.existsByUserIdAndOrganizationId(user.getId(), org.getId())) {
            OrganizationSubscription sub = OrganizationSubscription.builder()
                    .id(new OrganizationSubscriptionId(user.getId(), org.getId()))
                    .user(user)
                    .organization(org)
                    .subscribedAt(Instant.now())
                    .build();
            subscriptionRepo.save(sub);
        }

        return SubscriptionStatusDTO.builder()
                .organizationId(org.getId())
                .organizationName(org.getName())
                .isSubscribed(true)
                .build();
    }

    @Transactional
    public SubscriptionStatusDTO unsubscribe(Long organizationId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }

        Organization org = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new UnknownID("Organisation inconnue : " + organizationId));

        subscriptionRepo.findByUserIdAndOrganizationId(userDetails.getId(), organizationId)
                .ifPresent(subscriptionRepo::delete);

        return SubscriptionStatusDTO.builder()
                .organizationId(org.getId())
                .organizationName(org.getName())
                .isSubscribed(false)
                .build();
    }

    public SubscriptionStatusDTO isSubscribed(Long organizationId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return SubscriptionStatusDTO.builder()
                    .organizationId(organizationId)
                    .isSubscribed(false)
                    .build();
        }

        boolean subscribed = subscriptionRepo.existsByUserIdAndOrganizationId(userDetails.getId(), organizationId);
        return SubscriptionStatusDTO.builder()
                .organizationId(organizationId)
                .isSubscribed(subscribed)
                .build();
    }

    public List<User> getSubscribedUsers(Long organizationId) {
        return subscriptionRepo.findSubscribedUsersByOrganizationId(organizationId);
    }
}
