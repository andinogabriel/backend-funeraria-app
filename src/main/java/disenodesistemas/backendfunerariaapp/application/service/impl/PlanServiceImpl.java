package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.PlanService;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

  private final PlanCommandUseCase planCommandUseCase;
  private final PlanQueryUseCase planQueryUseCase;

  @Override
  public PlanResponseDto create(final PlanRequestDto planRequestDto) {
    return planCommandUseCase.create(planRequestDto);
  }

  @Override
  public PlanResponseDto update(final Long id, final PlanRequestDto planRequestDto) {
    return planCommandUseCase.update(id, planRequestDto);
  }

  @Override
  public PlanResponseDto findById(final Long id) {
    return planQueryUseCase.findById(id);
  }

  @Override
  public Plan findEntityById(final Long id) {
    return planQueryUseCase.findEntityById(id);
  }

  @Override
  public void delete(final Long id) {
    planCommandUseCase.delete(id);
  }

  @Override
  public List<PlanResponseDto> findAll() {
    return planQueryUseCase.findAll();
  }
}

