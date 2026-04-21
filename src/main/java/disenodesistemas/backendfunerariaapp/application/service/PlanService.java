package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.util.List;

public interface PlanService {

  PlanResponseDto create(PlanRequestDto dto);

  PlanResponseDto update(Long id, PlanRequestDto dto);

  void delete(Long id);

  List<PlanResponseDto> findAll();

  PlanResponseDto findById(Long id);

  Plan findEntityById(Long id);
}
