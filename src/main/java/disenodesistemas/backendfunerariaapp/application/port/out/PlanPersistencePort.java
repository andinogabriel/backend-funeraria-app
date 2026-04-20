package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.util.List;
import java.util.Optional;

public interface PlanPersistencePort {

  Optional<Plan> findById(Long id);

  List<Plan> findAllByOrderByIdDesc();

  List<Plan> findPlansContainingAnyOfThisItems(List<ItemEntity> items);

  Plan save(Plan plan);

  List<Plan> saveAll(List<Plan> plans);

  void delete(Plan plan);
}
