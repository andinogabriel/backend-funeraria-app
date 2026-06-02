package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgeBandRepository extends JpaRepository<AgeBandEntity, Long> {

  List<AgeBandEntity> findAllByOrderByDisplayOrderAsc();
}
