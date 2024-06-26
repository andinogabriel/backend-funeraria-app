package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.BrandTestDataFactory.getBrandEntityIdNull;
import static disenodesistemas.backendfunerariaapp.utils.BrandTestDataFactory.getBrandEntityWithItems;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.utils.BrandTestDataFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

  @Mock private BrandRepository brandRepository;
  @Mock private ProjectionFactory projectionFactory;
  @InjectMocks private BrandServiceImpl sut;

  private static BrandResponseDto brandResponseDto;
  private static BrandRequestDto brandRequestDto;

  @BeforeEach
  void setUp() {
    brandRequestDto = BrandTestDataFactory.getBrandRequestDto();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    brandResponseDto =
        projectionFactory.createProjection(BrandResponseDto.class, getBrandEntityIdNull());
  }

  @Test
  void getAllBrands() {
    final List<BrandResponseDto> brandResponsesDto = List.of(brandResponseDto);
    given(brandRepository.findAllByOrderByName()).willReturn(brandResponsesDto);

    final List<BrandResponseDto> result = sut.findAll();

    assertAll(
        () -> assertEquals(brandResponsesDto.size(), result.size()),
        () -> assertEquals(brandResponsesDto.get(0).getName(), result.get(0).getName()));
    verify(brandRepository, only()).findAllByOrderByName();
  }

  @Test
  void getBrandById() {
    final Long brandId = 1L;
    final BrandEntity expected = getBrandEntityIdNull();
    given(brandRepository.findById(brandId)).willReturn(Optional.of(expected));

    final BrandEntity result = sut.getBrandById(brandId);

    assertAll(
        () -> assertEquals(expected.getName(), result.getName()),
        () -> assertEquals(expected.getId(), result.getId()));
    verify(brandRepository, times(1)).findById(brandId);
  }

  @Test
  void getBrandByIdThrowsError() {
    final Long brandId = 2L;
    assertThrows(NotFoundException.class, () -> sut.getBrandById(brandId));
    verify(brandRepository, atMostOnce()).findById(brandId);
  }

  @Test
  void createBrand() {
    final BrandEntity expected = getBrandEntityIdNull();

    given(brandRepository.save(any(BrandEntity.class))).willReturn(expected);
    given(projectionFactory.createProjection(BrandResponseDto.class, expected))
        .willReturn(brandResponseDto);

    final BrandResponseDto result = sut.create(brandRequestDto);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getWebPage(), result.getWebPage()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(brandRepository, times(1)).save(any(BrandEntity.class));
  }

  @Test
  void updateBrand() {
    final BrandEntity expected = getBrandEntityIdNull();
    given(brandRepository.findById(expected.getId())).willReturn(Optional.of(expected));
    given(brandRepository.save(any(BrandEntity.class))).willReturn(expected);
    given(projectionFactory.createProjection(BrandResponseDto.class, expected))
        .willReturn(brandResponseDto);

    final BrandResponseDto result = sut.update(expected.getId(), brandRequestDto);

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getWebPage(), result.getWebPage()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(brandRepository, times(1)).save(any(BrandEntity.class));
  }

  @Test
  void deleteBrand() {
    final BrandEntity expected = getBrandEntityIdNull();
    given(brandRepository.findById(expected.getId())).willReturn(Optional.of(expected));

    sut.delete(expected.getId());

    verify(brandRepository, atLeastOnce()).delete(expected);
  }

  @Test
  @DisplayName(
      "Given a valid brand id with related items when delete method is called then it throws a ConflictException")
  void deleteBrandThrowsError() {
    final BrandEntity brandEntity = getBrandEntityWithItems();
    given(brandRepository.findById(brandEntity.getId())).willReturn(Optional.of(brandEntity));

    final ConflictException conflictException =
        assertThrows(ConflictException.class, () -> sut.delete(brandEntity.getId()));

    assertEquals("brand.error.invalid.delete", conflictException.getMessage());
    verify(brandRepository, never()).delete(brandEntity);
  }
}
