package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemsPlanRepository extends JpaRepository<ItemPlanEntity, Long> {
}
