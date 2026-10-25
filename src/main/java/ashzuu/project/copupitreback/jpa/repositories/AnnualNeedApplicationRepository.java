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

    List<AnnualNeedApplication> findByUserId(Long userId);

    Optional<AnnualNeedApplication> findByAnnualNeedIdAndUserId(Long annualNeedId, Long userId);
}
