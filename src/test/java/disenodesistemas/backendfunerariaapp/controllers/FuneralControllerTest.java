package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getFuneralEntity;
import static disenodesistemas.backendfunerariaapp.utils.FuneralTestDataFactory.getFuneralRequestDto;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.entities.Funeral;
import disenodesistemas.backendfunerariaapp.service.FuneralService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FuneralControllerTest
    extends AbstractControllerTest<FuneralRequestDto, FuneralResponseDto, Funeral, Long> {

  @Mock private FuneralService funeralService;
  @InjectMocks private FuneralController sut;
  private static final Long EXISTING_FUNERAL_IDENTIFIER = 1L;

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        funeralService::findAll);
    then(funeralService).should(times(1)).findAll();
  }

  @Test
  void findById() {
    testFindByID(funeralService::findById, sut::findById, EXISTING_FUNERAL_IDENTIFIER, responseDto);
    then(funeralService).should(times(1)).findById(EXISTING_FUNERAL_IDENTIFIER);
  }

  @Test
  void create() {
    testCreate(funeralService::create, sut::create, requestDto, responseDto);
    then(funeralService).should(times(1)).create(requestDto);
  }

  @Test
  void update() {
    testUpdate(
        funeralService::update, sut::update, EXISTING_FUNERAL_IDENTIFIER, requestDto, responseDto);
    then(funeralService).should(times(1)).update(EXISTING_FUNERAL_IDENTIFIER, requestDto);
  }

  @Test
  void delete() {
    testDelete(sut::delete, EXISTING_FUNERAL_IDENTIFIER, "DELETE FUNERAL");
    then(funeralService).should(times(1)).delete(EXISTING_FUNERAL_IDENTIFIER);
  }

  @Test
  void findFuneralsByUser() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findFuneralsByUser,
        () -> List.of(responseDto),
        funeralService::findFuneralsByUser);
    then(funeralService).should(times(1)).findFuneralsByUser();
  }

  @Override
  protected FuneralRequestDto getRequestDto() {
    return getFuneralRequestDto();
  }

  @Override
  protected Class<FuneralResponseDto> getResponseDtoClass() {
    return FuneralResponseDto.class;
  }

  @Override
  protected Funeral getEntity() {
    return getFuneralEntity();
  }
}
