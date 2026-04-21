package disenodesistemas.backendfunerariaapp.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Long> {
  List<Plan> findAllByOrderByIdDesc();
  @Query("SELECT DISTINCT p FROM Plan p JOIN p.itemsPlan ip JOIN ip.item i WHERE i IN :items")
  List<Plan> findPlansContainingAnyOfThisItems(@Param("items") List<ItemEntity> items);
}
