package disenodesistemas.backendfunerariaapp.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;

class BrandControllerTest {

  private BrandController sut;
  private BrandService brandService;
  private ProjectionFactory projectionFactory;

  @BeforeEach
  void setUp() {
    brandService = mock(BrandService.class);
    projectionFactory = mock(ProjectionFactory.class);
    sut = new BrandController(brandService, projectionFactory);
  }

  @DisplayName(
      "Given an valid id when call get brand by id method then return a brand projection dto")
  @Test
  void getBrandById() {
    val brandEntity = mock(BrandEntity.class);
    when(brandService.getBrandById(anyLong())).thenReturn(brandEntity);
    sut.getBrandById(anyLong());
    verify(brandService).getBrandById(anyLong());
    verify(projectionFactory).createProjection(BrandResponseDto.class, brandEntity);
  }
}
