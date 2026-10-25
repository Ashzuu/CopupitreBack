package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.AnnualNeedApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnnualNeedApplicationRepository extends JpaRepository<AnnualNeedApplication, Long> {

    @Query("""
        select a from AnnualNeedApplication a
        where a.annualNeed.id = :annualNeedId
        """)
    List<AnnualNeedApplication> findByAnnualNeedId(@Param("annualNeedId") Long annualNeedId);

    boolean existsByAnnualNeedIdAndUserId(Long annualNeedId, Long userId);

    long countByAnnualNeedIdAndStatus(Long annualNeedId, String status);

    @Query("""
        select a from AnnualNeedApplication a
        left join fetch a.annualNeed n
        left join fetch n.organization o
        left join fetch o.organizationAdmins oa
        left join fetch oa.user
        where a.user.id = :userId
        order by a.appliedAt desc
        """)
    List<AnnualNeedApplication> findByUserId(@Param("userId") Long userId);

    Optional<AnnualNeedApplication> findByAnnualNeedIdAndUserId(Long annualNeedId, Long userId);
}
