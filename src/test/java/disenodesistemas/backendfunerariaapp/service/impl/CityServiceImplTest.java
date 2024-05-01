package disenodesistemas.backendfunerariaapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.utils.CityTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.ProvinceTestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

  @Mock private CityRepository cityRepository;
  @InjectMocks private CityServiceImpl sut;

  private CityResponseDto cityResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    cityResponseDto =
        projectionFactory.createProjection(
            CityResponseDto.class, CityTestDataFactory.getCityEntity());
  }

  @Test
  void getCityById() {
    final CityEntity expected = CityTestDataFactory.getCityEntity();
    final Long id = expected.getId();

    given(cityRepository.getById(id)).willReturn(Optional.ofNullable(cityResponseDto));

    final CityResponseDto actualResult = sut.getCityById(id);

    assertAll(
        () -> assertEquals(expected.getId(), actualResult.getId()),
        () -> assertEquals(expected.getZipCode(), actualResult.getZipCode()),
        () -> assertEquals(expected.getName(), actualResult.getName()),
        () -> assertEquals(expected.getProvince().getName(), actualResult.getProvince().getName()));
    verify(cityRepository, times(1)).getById(id);
  }

  @Test
  void getCitiesByProvinceId() {
    final ProvinceEntity provinceEntity = ProvinceTestDataFactory.getChacoProvince();
    final List<CityResponseDto> expected = List.of(cityResponseDto);
    given(cityRepository.findByProvinceOrderByName(provinceEntity)).willReturn(expected);

    final List<CityResponseDto> actualResult = sut.getCitiesByProvinceId(provinceEntity.getId());

    assertAll(
        () -> assertEquals(expected.size(), actualResult.size()),
        () -> assertEquals(expected.get(0).getId(), actualResult.get(0).getId()),
        () -> assertEquals(expected.get(0).getName(), actualResult.get(0).getName()),
        () -> assertEquals(expected.get(0).getZipCode(), actualResult.get(0).getZipCode()));
    verify(cityRepository, times(1)).findByProvinceOrderByName(provinceEntity);
  }
}
