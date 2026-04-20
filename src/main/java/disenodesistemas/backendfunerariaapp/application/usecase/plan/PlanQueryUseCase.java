package disenodesistemas.backendfunerariaapp.application.usecase.plan;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanQueryUseCase {

  private final PlanPersistencePort planPersistencePort;
  private final PlanMapper planMapper;

  @Transactional(readOnly = true)
  public PlanResponseDto findById(final Long id) {
    return planMapper.toDto(findPlanById(id));
  }

  @Transactional(readOnly = true)
  public Plan findEntityById(final Long id) {
    return findPlanById(id);
  }

  @Transactional(readOnly = true)
  public List<PlanResponseDto> findAll() {
    return planPersistencePort.findAllByOrderByIdDesc().stream().map(planMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Plan findPlanById(final Long id) {
    return planPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("plan.error.not.found"));
  }
}
