package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.ProvinceTestDataFactory.getChacoProvince;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ProvinceRepository;
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
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProvinceServiceImplTest {

  @Mock private ProvinceRepository provinceRepository;
  @InjectMocks ProvinceServiceImpl sut;
  private ProvinceResponseDto provinceResponseDto;
  private ProvinceEntity provinceEntity;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    provinceEntity = getChacoProvince();
    provinceResponseDto =
        projectionFactory.createProjection(ProvinceResponseDto.class, provinceEntity);
  }

  @Test
  void getAllProvinces() {
    final List<ProvinceResponseDto> expectedResult = List.of(provinceResponseDto);
    given(provinceRepository.findAllByOrderByName()).willReturn(expectedResult);

    final List<ProvinceResponseDto> actualResult = sut.getAllProvinces();

    assertAll(
        () -> assertFalse(actualResult.isEmpty()), () -> assertSame(expectedResult, actualResult));
    then(provinceRepository).should(times(1)).findAllByOrderByName();
  }

  @Test
  void getProvinceById() {
    final Long id = provinceEntity.getId();
    given(provinceRepository.findById(id)).willReturn(Optional.of(provinceEntity));

    final ProvinceEntity actualResult = sut.getProvinceById(id);

    assertAll(
        () -> assertEquals(provinceEntity.getId(), actualResult.getId()),
        () -> assertEquals(provinceEntity.getName(), actualResult.getName()));
    then(provinceRepository).should(times(1)).findById(id);
  }

  @Test
  void getProvinceByIdThrowsNotFoundException() {
    final Long NON_EXISTENT_PROVINCE_ID = 911L;
    final String NOT_FOUND_ERROR_MSG = "province.error.not.found";
    given(provinceRepository.findById(NON_EXISTENT_PROVINCE_ID))
        .willThrow(new NotFoundException(NOT_FOUND_ERROR_MSG));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.getProvinceById(NON_EXISTENT_PROVINCE_ID));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals(NOT_FOUND_ERROR_MSG, actualResult.getMessage()));
    then(provinceRepository).should(times(1)).findById(NON_EXISTENT_PROVINCE_ID);
  }
}
