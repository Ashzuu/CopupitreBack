package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    @Query("""
        select o from Organization o
        join fetch o.organizationAdmins admin
        where admin.user.id = :id
        """)
    List<Organization> getAllByUserId(@Param("id") Long id);

    @Query("""
        select count(admin) > 0 from OrganizationAdmin admin
        where admin.user.id = :userId and admin.organization.id = :orgId
        """)
    boolean isAdminOfOrganization(@Param("userId") Long userId, @Param("orgId") Long orgId);
}
