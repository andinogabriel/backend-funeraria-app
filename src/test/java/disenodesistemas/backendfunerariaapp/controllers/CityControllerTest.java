package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.CityTestDataFactory.getCityDto;
import static disenodesistemas.backendfunerariaapp.utils.CityTestDataFactory.getCityEntity;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.CityDto;
import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.service.CityService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CityControllerTest
    extends AbstractControllerTest<CityDto, CityResponseDto, CityEntity, Long> {

  @Mock private CityService cityService;
  @InjectMocks private CityController sut;
  private static final Long CITY_ID = 7871L;

  @Test
  void getCityById() {
    testFindByID(cityService::findById, sut::findById, CITY_ID, responseDto);
    then(cityService).should(times(1)).findById(CITY_ID);
  }

  @Test
  void getCitiesByProvinceId() {
    final Long PROVINCE_ID = entity.getProvince().getId();
    testFindAll(
        () -> List.of(responseDto),
        () -> sut.findByProvinceId(PROVINCE_ID),
        () -> List.of(responseDto),
        () -> cityService.findByProvinceId(PROVINCE_ID));
    then(cityService).should(times(1)).findByProvinceId(PROVINCE_ID);
  }

  @Override
  protected CityDto getRequestDto() {
    return getCityDto();
  }

  @Override
  protected Class<CityResponseDto> getResponseDtoClass() {
    return CityResponseDto.class;
  }

  @Override
  protected CityEntity getEntity() {
    return getCityEntity();
  }
}
