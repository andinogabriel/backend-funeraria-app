package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeathCauseRepository extends JpaRepository<DeathCauseEntity, Long> {
    List<DeathCauseEntity> findAllByOrderByNameAsc();
}
