package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;

import java.util.List;

public interface PlanService extends CommonService<PlanResponseDto, PlanRequestDto, Long> {
  PlanResponseDto findById(Long id);

  Plan findEntityById(Long id);

  void updatePlansPrice(List<ItemEntity> items);
}
