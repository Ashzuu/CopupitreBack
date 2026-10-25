package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.contextType = :contextType AND m.contextId = :contextId ORDER BY m.createdAt ASC")
    List<Message> findByContextOrderByCreatedAtAsc(
            @Param("contextType") String contextType,
            @Param("contextId") Long contextId);
}
