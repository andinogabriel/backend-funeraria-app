package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeathCause;
import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeathCauseEntity;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.service.DeathCauseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class DeathCauseControllerTest
    extends AbstractControllerTest<DeathCauseDto, DeathCauseResponseDto, DeathCauseEntity, Long> {

  @Mock private DeathCauseService deathCauseService;
  @InjectMocks private DeathCauseController sut;
  private static final Long EXISTING_DEATH_CAUSE_IDENTIFIER = 1L;

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        deathCauseService::findAll);
    then(deathCauseService).should(times(1)).findAll();
  }

  @Test
  void findByDni() {
    testFindByID(
        deathCauseService::findById, sut::findById, EXISTING_DEATH_CAUSE_IDENTIFIER, responseDto);
    then(deathCauseService).should(times(1)).findById(EXISTING_DEATH_CAUSE_IDENTIFIER);
  }

  @Test
  void create() {
    testCreate(deathCauseService::create, sut::create, requestDto, responseDto);
    then(deathCauseService).should(times(1)).create(requestDto);
  }

  @Test
  void update() {
    testUpdate(
        deathCauseService::update,
        sut::update,
        EXISTING_DEATH_CAUSE_IDENTIFIER,
        requestDto,
        responseDto);
    then(deathCauseService).should(times(1)).update(EXISTING_DEATH_CAUSE_IDENTIFIER, requestDto);
  }

  @Test
  void delete() {
    testDelete(sut::delete, EXISTING_DEATH_CAUSE_IDENTIFIER, "DELETE DEATH CAUSE");
    then(deathCauseService).should(times(1)).delete(EXISTING_DEATH_CAUSE_IDENTIFIER);
  }

  @Override
  protected DeathCauseDto getRequestDto() {
    return getDeathCause();
  }

  @Override
  protected Class<DeathCauseResponseDto> getResponseDtoClass() {
    return DeathCauseResponseDto.class;
  }

  @Override
  protected DeathCauseEntity getEntity() {
    return getDeathCauseEntity();
  }
}
