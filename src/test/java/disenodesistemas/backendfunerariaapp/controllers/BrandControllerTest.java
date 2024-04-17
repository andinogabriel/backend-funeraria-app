package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.dto.BrandDtoMother.getBrandEntity;
import static disenodesistemas.backendfunerariaapp.dto.BrandDtoMother.getBrandRequestDto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class BrandControllerTest {

  @Mock private BrandService brandService;
  @Mock private ProjectionFactory projectionFactory;
  @InjectMocks private BrandController sut;
  private BrandResponseDto brandResponseDto;
  private BrandRequestDto brandRequestDto;

  @BeforeEach
  void setUp() {
    this.projectionFactory = new SpelAwareProxyProjectionFactory();
    sut = new BrandController(brandService, projectionFactory);
    this.brandResponseDto =
        projectionFactory.createProjection(BrandResponseDto.class, getBrandEntity());
    this.brandRequestDto = getBrandRequestDto();
  }

  @DisplayName(
      "Given an valid id when call get brand by id method then return a brand projection dto")
  @Test
  void getBrandById() {
    final Long id = 1L;
    final ResponseEntity<BrandResponseDto> expectedResponse = ResponseEntity.ok(brandResponseDto);
    when(brandService.getBrandById(id)).thenReturn(getBrandEntity());

    final ResponseEntity<BrandResponseDto> actualResponse = sut.getBrandById(id);

    assertAll(
        () -> assertEquals(HttpStatus.OK, actualResponse.getStatusCode()),
        () -> assertNotNull(actualResponse.getBody()));
    verify(brandService, only()).getBrandById(id);
  }

  @Test
  void getAllBrands() {
    final List<BrandResponseDto> expectedResponse = List.of(brandResponseDto);
    when(brandService.findAll()).thenReturn(expectedResponse);

    final List<BrandResponseDto> actualResponse = sut.getAllBrandes();

    assertEquals(expectedResponse, actualResponse);
    verify(brandService, only()).findAll();
  }

  @Test
  void createBrand() {
    when(brandService.create(brandRequestDto)).thenReturn(brandResponseDto);

    final BrandResponseDto actualResponse = sut.createBrand(brandRequestDto);

    assertEquals(brandResponseDto, actualResponse);
    verify(brandService, only()).create(brandRequestDto);
  }

  @Test
  void updateBrand() {
    final Long id = 1L;
    when(brandService.update(id, brandRequestDto)).thenReturn(brandResponseDto);

    final BrandResponseDto actualResponse = sut.updateBrand(id, brandRequestDto);

    assertEquals(brandResponseDto, actualResponse);
    verify(brandService, only()).update(id, brandRequestDto);
  }

  @Test
  void deleteBrand() {
    final Long id = 1L;

    final OperationStatusModel expectedResponse =
        OperationStatusModel.builder().name("DELETE").result("SUCCESS").build();

    final OperationStatusModel actualResponse = sut.deleteBrand(id);

    assertEquals(expectedResponse, actualResponse);
    verify(brandService, only()).delete(id);
  }
}
