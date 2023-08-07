package disenodesistemas.backendfunerariaapp.repository;

import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<PlanResponseDto> findAllProjectedByOrderByIdDesc();

    //@Query("SELECT DISTINCT p FROM Plan p WHERE p.id IN (SELECT ip.plan.id FROM ItemPlanEntity ip WHERE ip.item IN :items)")
    @Query("SELECT DISTINCT p FROM Plan p JOIN p.itemsPlan ip JOIN ip.item i WHERE i IN :items")
    List<Plan> findPlansContainingAnyOfThisItems(@Param("items") List<ItemEntity> items);

}
