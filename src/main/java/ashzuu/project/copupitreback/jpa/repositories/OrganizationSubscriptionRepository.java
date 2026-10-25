package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationSubscription;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationSubscriptionId;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationSubscriptionRepository extends JpaRepository<OrganizationSubscription, OrganizationSubscriptionId> {

    boolean existsByUserIdAndOrganizationId(Long userId, Long organizationId);

    Optional<OrganizationSubscription> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    List<OrganizationSubscription> findByUserId(Long userId);

    List<OrganizationSubscription> findByOrganizationId(Long organizationId);

    @Query("SELECT s.user FROM OrganizationSubscription s WHERE s.organization.id = :orgId")
    List<User> findSubscribedUsersByOrganizationId(@Param("orgId") Long orgId);
}
