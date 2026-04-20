package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.persistence.repository.PlanRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaPlanPersistenceAdapter implements PlanPersistencePort {

  private final PlanRepository planRepository;

  @Override
  public Optional<Plan> findById(final Long id) {
    return planRepository.findById(id);
  }

  @Override
  public List<Plan> findAllByOrderByIdDesc() {
    return planRepository.findAllByOrderByIdDesc();
  }

  @Override
  public List<Plan> findPlansContainingAnyOfThisItems(final List<ItemEntity> items) {
    return planRepository.findPlansContainingAnyOfThisItems(items);
  }

  @Override
  @Transactional
  public Plan save(final Plan plan) {
    return planRepository.save(plan);
  }

  @Override
  @Transactional
  public List<Plan> saveAll(final List<Plan> plans) {
    return planRepository.saveAll(plans);
  }

  @Override
  @Transactional
  public void delete(final Plan plan) {
    planRepository.delete(plan);
  }
}
