package disenodesistemas.backendfunerariaapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.dto.BrandRequestDtoMother;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.BrandEntityMother;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class BrandServiceImplTest {

  @Mock private BrandRepository brandRepository;
  @Mock private ProjectionFactory projectionFactory;
  @InjectMocks private BrandServiceImpl sut;

  private BrandResponseDto brandResponseDto;

  @BeforeEach
  void setUp() {
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    brandResponseDto =
        projectionFactory.createProjection(
            BrandResponseDto.class, BrandEntityMother.getBrandEntity());
  }

  @Test
  void getAllBrands() {
    final List<BrandResponseDto> brandResponsesDto = List.of(brandResponseDto);
    given(brandRepository.findAllByOrderByName()).willReturn(brandResponsesDto);

    final List<BrandResponseDto> result = sut.getAllBrands();

    assertAll(
        () -> assertEquals(brandResponsesDto.size(), result.size()),
        () -> assertEquals(brandResponsesDto.get(0).getName(), result.get(0).getName()));
    verify(brandRepository, only()).findAllByOrderByName();
  }

  @Test
  void getBrandById() {
    final Long brandId = 1L;
    final BrandEntity expected = BrandEntityMother.getBrandEntity();
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
    final BrandEntity expected = BrandEntityMother.getBrandEntity();

    given(brandRepository.save(any(BrandEntity.class))).willReturn(expected);
    given(projectionFactory.createProjection(BrandResponseDto.class, expected))
        .willReturn(brandResponseDto);

    final BrandResponseDto result = sut.createBrand(BrandRequestDtoMother.getBrandRequestDto());

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getWebPage(), result.getWebPage()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(brandRepository, times(1)).save(any(BrandEntity.class));
  }

  @Test
  void updateBrand() {
    final BrandEntity expected = BrandEntityMother.getBrandEntity();
    given(brandRepository.findById(expected.getId())).willReturn(Optional.of(expected));
    given(brandRepository.save(any(BrandEntity.class))).willReturn(expected);
    given(projectionFactory.createProjection(BrandResponseDto.class, expected))
        .willReturn(brandResponseDto);

    final BrandResponseDto result =
        sut.updateBrand(expected.getId(), BrandRequestDtoMother.getBrandRequestDto());

    assertAll(
        () -> assertEquals(expected.getId(), result.getId()),
        () -> assertEquals(expected.getWebPage(), result.getWebPage()),
        () -> assertEquals(expected.getName(), result.getName()));
    verify(brandRepository, times(1)).save(any(BrandEntity.class));
  }

  @Test
  void deleteBrand() {
    final BrandEntity expected = BrandEntityMother.getBrandEntity();
    given(brandRepository.findById(expected.getId())).willReturn(Optional.of(expected));
    sut.deleteBrand(expected.getId());
    verify(brandRepository, only()).delete(expected);
  }
}
