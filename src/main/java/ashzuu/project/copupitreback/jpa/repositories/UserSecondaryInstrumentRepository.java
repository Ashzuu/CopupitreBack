package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrument;
import ashzuu.project.copupitreback.jpa.entities.user.UserSecondaryInstrumentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSecondaryInstrumentRepository extends JpaRepository<UserSecondaryInstrument, UserSecondaryInstrumentId> {
    List<UserSecondaryInstrument> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
