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

    List<Application> findByUserId(Long userId);

    Optional<Application> findByRequestIdAndUserId(Long requestId, Long userId);
}
