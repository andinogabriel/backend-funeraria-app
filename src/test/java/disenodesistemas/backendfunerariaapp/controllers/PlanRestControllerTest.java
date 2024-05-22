package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.PlanTestDataFactory.getPlanEntity;
import static disenodesistemas.backendfunerariaapp.utils.PlanTestDataFactory.getPlanRequest;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class PlanRestControllerTest
    extends AbstractControllerTest<PlanRequestDto, PlanResponseDto, Plan, Long> {

  @Mock private PlanService planService;
  @InjectMocks private PlanRestController sut;
  private final Long EXISTING_PLAN_IDENTIFIER = 1L;

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto), sut::findAll, () -> List.of(responseDto), planService::findAll);
    then(planService).should(times(1)).findAll();
  }

  @Test
  void create() {
    testCreate(planService::create, sut::create, requestDto, responseDto);
    then(planService).should(times(1)).create(requestDto);
  }

  @Test
  void delete() {
    testDelete(sut::delete, EXISTING_PLAN_IDENTIFIER, "DELETE PLAN");
    then(planService).should(times(1)).delete(EXISTING_PLAN_IDENTIFIER);
  }

  @Test
  void update() {
    testUpdate(planService::update, sut::update, EXISTING_PLAN_IDENTIFIER, requestDto, responseDto);
    then(planService).should(times(1)).update(EXISTING_PLAN_IDENTIFIER, requestDto);
  }

  @Override
  protected PlanRequestDto getRequestDto() {
    return getPlanRequest();
  }

  @Override
  protected Class<PlanResponseDto> getResponseDtoClass() {
    return PlanResponseDto.class;
  }

  @Override
  protected Plan getEntity() {
    return getPlanEntity();
  }
}
