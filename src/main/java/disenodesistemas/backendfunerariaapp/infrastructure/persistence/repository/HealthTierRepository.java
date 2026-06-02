package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthTierRepository extends JpaRepository<HealthTierEntity, Long> {

  List<HealthTierEntity> findAllByOrderByDisplayOrderAsc();

  Optional<HealthTierEntity> findByCode(String code);
}
