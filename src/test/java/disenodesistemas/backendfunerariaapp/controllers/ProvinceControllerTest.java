package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.ProvinceTestDataFactory.getChacoProvince;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.service.ProvinceService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProvinceControllerTest {

  @Mock private ProvinceService provinceService;
  @InjectMocks private ProvinceController sut;
  private ProvinceResponseDto provinceResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    provinceResponseDto =
        projectionFactory.createProjection(ProvinceResponseDto.class, getChacoProvince());
  }

  @Test
  void findAll() {
    final List<ProvinceResponseDto> expectedList = List.of(provinceResponseDto);
    given(provinceService.getAllProvinces()).willReturn(expectedList);

    final ResponseEntity<List<ProvinceResponseDto>> actualResult = sut.findAll();

    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResult.getStatusCode()),
        () -> assertEquals(expectedList, actualResult.getBody()));
    then(provinceService).should(times(1)).getAllProvinces();
  }
}
