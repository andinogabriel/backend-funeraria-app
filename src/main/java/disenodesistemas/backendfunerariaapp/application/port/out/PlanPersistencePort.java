package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanPersistencePort {

  Optional<Plan> findById(Long id);

  List<Plan> findAllByOrderByIdDesc();

  List<Plan> findPlansContainingAnyOfThisItems(List<ItemEntity> items);

  Plan save(Plan plan);

  List<Plan> saveAll(List<Plan> plans);

  /**
   * Admin-only paginated read of soft-deleted plans. See
   * {@code PlanRepository#findAllDeleted} for the filter semantics; the port mirrors
   * that contract so the use-case layer never reaches into Spring Data directly.
   */
  Page<Plan> findAllDeleted(
      String name, String deletedBy, Instant deletedFrom, Instant deletedTo, Pageable pageable);
}
