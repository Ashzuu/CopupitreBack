package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.organization.Absence;
import ashzuu.project.copupitreback.jpa.entities.organization.AbsenceStatus;
import ashzuu.project.copupitreback.jpa.entities.organization.EventType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface AbsenceRepository extends JpaRepository<Absence, Long>, JpaSpecificationExecutor<Absence> {

    default List<Absence> findByFilters(
            Long orgId,
            LocalDate startDate,
            LocalDate endDate,
            EventType eventType,
            AbsenceStatus status,
            Long musicianId
    ) {
        Specification<Absence> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), endDate));
            }
            if (eventType != null) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (musicianId != null) {
                predicates.add(cb.equal(root.get("musician").get("id"), musicianId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return findAll(spec, Sort.by(Sort.Order.desc("eventDate"), Sort.Order.desc("createdAt")));
    }

    default List<Absence> findByOrganizationIdAndDateRange(
            Long orgId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Specification<Absence> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return findAll(spec);
    }

    List<Absence> findByMusicianIdOrderByEventDateDesc(Long musicianId);
}

