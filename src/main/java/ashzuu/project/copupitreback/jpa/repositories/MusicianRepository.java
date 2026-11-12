package ashzuu.project.copupitreback.jpa.repositories;

import ashzuu.project.copupitreback.jpa.entities.organization.Musician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MusicianRepository extends JpaRepository<Musician, Long> {

    Optional<Musician> findByLastNameIgnoreCaseAndFirstNameIgnoreCaseAndEmailIgnoreCase(
            String lastName, String firstName, String email
    );

    Optional<Musician> findByEmailIgnoreCase(String email);

    @Query("SELECT m FROM Musician m JOIN m.organizations o WHERE o.id = :orgId ORDER BY m.lastName ASC, m.firstName ASC")
    List<Musician> findAllByOrganizationId(@Param("orgId") Long orgId);

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM Musician m JOIN m.organizations o
        WHERE o.id = :orgId AND m.id = :musicianId
        """)
    boolean isMusicianInOrganization(@Param("orgId") Long orgId, @Param("musicianId") Long musicianId);

    @Query("""
        SELECT m FROM Musician m JOIN m.organizations o
        WHERE o.id = :orgId AND m.user.id = :userId
        """)
    Optional<Musician> findByOrganizationIdAndUserId(@Param("orgId") Long orgId, @Param("userId") Long userId);
}
