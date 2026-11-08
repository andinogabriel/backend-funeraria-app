package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.IdempotencyRecord;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

  Optional<IdempotencyRecord> findByPrincipalAndIdempotencyKey(String principal, String idempotencyKey);

  @Modifying
  @Query("delete from IdempotencyRecord r where r.expiresAt <= :now")
  int deleteExpired(@Param("now") Instant now);
}
