package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.AnnualNeeds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnualNeedRepository extends JpaRepository<AnnualNeeds, Long> {

    @Query("select n from AnnualNeeds n left join fetch n.organization o")
    List<AnnualNeeds> findAllWithOrganization();

    @Query("""
        select n from AnnualNeeds n 
        join n.organization o 
        join o.organizationAdmins oa 
        where oa.user.id = :userId
        """)
    List<AnnualNeeds> findByAdminUserId(@Param("userId") Long userId);

    @Query("select n from AnnualNeeds n left join fetch n.organization o where o.id = :organizationId")
    List<AnnualNeeds> findByOrganizationId(@Param("organizationId") Long organizationId);
}
