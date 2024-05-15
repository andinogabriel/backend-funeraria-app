package disenodesistemas.backendfunerariaapp.controllers;

import static disenodesistemas.backendfunerariaapp.utils.BrandTestDataFactory.getBrandEntity;
import static disenodesistemas.backendfunerariaapp.utils.BrandTestDataFactory.getBrandRequestDto;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class BrandControllerTest
    extends AbstractControllerTest<BrandRequestDto, BrandResponseDto, BrandEntity, Long> {

  @Mock private BrandService brandService;
  @InjectMocks private BrandController sut;
  private static final Long EXISTING_BRAND_ID = 1L;

  @DisplayName(
      "Given an valid id when call get brand by id method then return a brand projection dto")
  @Test
  void getBrandById() {
    testFindByID(brandService::findById, sut::findById, EXISTING_BRAND_ID, responseDto);
    then(brandService).should(times(1)).findById(EXISTING_BRAND_ID);
  }

  @Test
  void getAllBrands() {
    testFindAll(
        () -> List.of(responseDto),
        sut::findAll,
        () -> List.of(responseDto),
        brandService::findAll);
    then(brandService).should(times(1)).findAll();
  }

  @Test
  void createBrand() {
    testCreate(brandService::create, sut::create, requestDto, responseDto);
    then(brandService).should(times(1)).create(requestDto);
  }

  @Test
  void updateBrand() {
    testUpdate(brandService::update, sut::update, EXISTING_BRAND_ID, requestDto, responseDto);
    then(brandService).should(times(1)).update(EXISTING_BRAND_ID, requestDto);
  }

  @Test
  void deleteBrand() {
    testDelete(sut::delete, EXISTING_BRAND_ID, "DELETE BRAND");
    then(brandService).should(times(1)).delete(EXISTING_BRAND_ID);
  }

  @Override
  protected BrandRequestDto getRequestDto() {
    return getBrandRequestDto();
  }

  @Override
  protected Class<BrandResponseDto> getResponseDtoClass() {
    return BrandResponseDto.class;
  }

  @Override
  protected BrandEntity getEntity() {
    return getBrandEntity();
  }
}
