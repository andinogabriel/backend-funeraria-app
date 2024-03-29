package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;

import java.util.List;

public interface PlanService {
    PlanResponseDto create(final PlanRequestDto planResponseDto);
    PlanResponseDto update(final Long id, final PlanRequestDto planResponseDto);
    PlanResponseDto getById(final Long id);
    Plan findById(Long id);
    void delete(final Long id);
    List<PlanResponseDto> findAll();
    void updatePlansPrice(List<ItemEntity> items);
}
