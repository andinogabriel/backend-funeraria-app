package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;

import java.util.List;

public interface PlanService extends CommonService<PlanResponseDto, PlanRequestDto, Long> {
  PlanResponseDto getById(final Long id);

  Plan findById(Long id);

  void updatePlansPrice(List<ItemEntity> items);
}
