package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.CityEntityMother;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntityMother;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class CityServiceImplTest {

  @Mock private CityRepository cityRepository;
  @InjectMocks private CityServiceImpl sut;

  private CityResponseDto cityResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    cityResponseDto =
        projectionFactory.createProjection(CityResponseDto.class, CityEntityMother.getCityEntity());
  }

  @Test
  void getCityById() {
    final Long id = CityEntityMother.getCityEntity().getId();
    final CityEntity expected = CityEntityMother.getCityEntity();
    given(cityRepository.getById(id)).willReturn(Optional.ofNullable(cityResponseDto));

    final CityResponseDto result = sut.getCityById(id);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getZipCode(), result.getZipCode()),
        () -> assertEquals(expected.getName(), result.getName()),
        () -> assertEquals(expected.getProvince().getName(), result.getProvince().getName()));
    verify(cityRepository, times(1)).getById(id);
  }

  @Test
  void getCitiesByProvinceId() {
    final ProvinceEntity provinceEntity = ProvinceEntityMother.getChacoProvince();
    final List<CityResponseDto> expected = List.of(cityResponseDto);
    given(cityRepository.findByProvinceOrderByName(provinceEntity)).willReturn(expected);

    final List<CityResponseDto> result = sut.getCitiesByProvinceId(provinceEntity.getId());

    assertAll(
        () -> assertEquals(expected.size(), result.size()),
        () -> assertEquals(expected.get(0).getId(), result.get(0).getId()),
        () -> assertEquals(expected.get(0).getName(), result.get(0).getName()),
        () -> assertEquals(expected.get(0).getZipCode(), result.get(0).getZipCode()));
    verify(cityRepository, times(1)).findByProvinceOrderByName(provinceEntity);
  }
}
