package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedEntity;
import static disenodesistemas.backendfunerariaapp.utils.DeceasedTestDataFactory.getDeceasedRequestDto;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.entities.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.service.DeceasedService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class DeceasedControllerTest
    extends AbstractControllerTest<
        DeceasedRequestDto, DeceasedResponseDto, DeceasedEntity, Integer> {

  @Mock private DeceasedService deceasedService;
  @InjectMocks private DeceasedController sut;
  private static final Integer DNI = 12345678;
  private static final Integer NOT_FOUND_DNI = 987654321;

  @Test
  void findAll() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        deceasedService::findAll);
    then(deceasedService).should(times(1)).findAll();
  }

  @Test
  void findByDni() {
    testFindByID(deceasedService::findById, sut::findByDni, DNI, responseDto);
    then(deceasedService).should(times(1)).findById(DNI);
  }

  @Test
  void findByDniNotFound() {
    given(deceasedService.findById(NOT_FOUND_DNI)).willThrow(NotFoundException.class);
    assertThrows(NotFoundException.class, () -> sut.findByDni(NOT_FOUND_DNI));
  }

  @Test
  void create() {
    testCreate(deceasedService::create, sut::create, requestDto, responseDto);
    then(deceasedService).should(times(1)).create(requestDto);
  }

  @Test
  void update() {
    testUpdate(deceasedService::update, sut::update, DNI, requestDto, responseDto);
    then(deceasedService).should(times(1)).update(DNI, requestDto);
  }

  @Test
  void delete() {
    testDelete(sut::delete, DNI, "DELETE DECEASED");
    then(deceasedService).should(times(1)).delete(DNI);
  }

  @Override
  protected DeceasedRequestDto getRequestDto() {
    return getDeceasedRequestDto();
  }

  @Override
  protected Class<DeceasedResponseDto> getResponseDtoClass() {
    return DeceasedResponseDto.class;
  }

  @Override
  protected DeceasedEntity getEntity() {
    return getDeceasedEntity();
  }
}
