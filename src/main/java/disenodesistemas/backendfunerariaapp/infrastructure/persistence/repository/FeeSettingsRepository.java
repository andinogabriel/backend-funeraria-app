package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeSettingsRepository extends JpaRepository<FeeSettingsEntity, Long> {}
