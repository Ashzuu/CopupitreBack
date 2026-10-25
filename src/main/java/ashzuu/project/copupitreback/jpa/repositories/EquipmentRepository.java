package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.equipment.EquipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<EquipmentRequest, Long> {
    List<EquipmentRequest> findByOrganizationId(Long organizationId);
    List<EquipmentRequest> findByOrganizationIdIn(Collection<Long> organizationIds);
    List<EquipmentRequest> findByStatus(String status);
}
