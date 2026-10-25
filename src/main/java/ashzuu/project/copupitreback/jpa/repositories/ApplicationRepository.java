package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("""
        select a from Application a
        where a.request.id = :id
        """)
    List<Application> findAllByIdRequest(@Param("id") Long id);

    boolean existsByRequestIdAndUserId(Long requestId, Long userId);

    long countByRequestIdAndStatus(Long requestId, String status);

    @Query("""
        select a from Application a
        left join fetch a.request r
        left join fetch r.organization o
        left join fetch o.organizationAdmins oa
        left join fetch oa.user
        where a.user.id = :userId
        order by a.appliedAt desc
        """)
    List<Application> findByUserId(@Param("userId") Long userId);

    Optional<Application> findByRequestIdAndUserId(Long requestId, Long userId);
}
