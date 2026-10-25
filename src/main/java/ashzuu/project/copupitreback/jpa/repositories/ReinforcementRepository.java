package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReinforcementRepository extends JpaRepository<ReinforcementRequest, Long> {

    List<ReinforcementRequest> findByOrganizationId(Long organizationId);

    @Query("""
        SELECT r FROM ReinforcementRequest r
        JOIN r.organization o
        JOIN o.organizationAdmins a
        WHERE a.user.id = :userId
        ORDER BY r.createdAt DESC
        """)
    List<ReinforcementRequest> findByAdminUserId(@Param("userId") Long userId);
}
