package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.ReinforcementRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReinforcementRepository extends JpaRepository<ReinforcementRequest, Long> {
}
