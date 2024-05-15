package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory.getSupplierEntity;
import static disenodesistemas.backendfunerariaapp.utils.SupplierTestDataFactory.getSupplierRequestDto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.repository.SupplierRepository;
import disenodesistemas.backendfunerariaapp.service.EntityProcessor;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
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
class SupplierServiceImplTest {

  @Mock private SupplierRepository supplierRepository;
  @Mock private ProjectionFactory projectionFactory;
  @Mock private AbstractConverter<MobileNumberEntity, MobileNumberRequestDto> mobileNumberConverter;
  @Mock private AbstractConverter<AddressEntity, AddressRequestDto> addressConverter;

  @Mock
  private EntityProcessor<MobileNumberEntity, MobileNumberRequestDto> mobileNumberEntityProcessor;

  @Mock private EntityProcessor<AddressEntity, AddressRequestDto> addressEntityProcessor;
  @InjectMocks private SupplierServiceImpl sut;

  private static SupplierRequestDto supplierRequestDto;
  private static SupplierResponseDto supplierResponseDto;
  private static final String EXISTING_NIF = "NIF123ASD";

  @BeforeEach
  void setUp() {
    supplierRequestDto = getSupplierRequestDto();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    supplierResponseDto =
        projectionFactory.createProjection(SupplierResponseDto.class, getSupplierEntity());
  }

  @Test
  void findAll() {
    final List<SupplierResponseDto> expectedResult = List.of(supplierResponseDto);
    given(supplierRepository.findAllProjectedByOrderByIdDesc()).willReturn(expectedResult);

    final List<SupplierResponseDto> actualResult = sut.findAll();

    assertAll(
        () -> assertFalse(actualResult.isEmpty()),
        () -> assertEquals(expectedResult.get(0).getName(), actualResult.get(0).getName()),
        () -> assertEquals(expectedResult.get(0).getNif(), actualResult.get(0).getNif()));
    then(supplierRepository).should(times(1)).findAllProjectedByOrderByIdDesc();
  }

  @Test
  void create() {
    final SupplierEntity expectedResult = getSupplierEntity();
    given(supplierRepository.save(expectedResult)).willReturn(expectedResult);
    given(projectionFactory.createProjection(SupplierResponseDto.class, expectedResult))
        .willReturn(supplierResponseDto);

    final SupplierResponseDto actualResult = sut.create(supplierRequestDto);

    supplierAsserts(expectedResult, actualResult);
    then(supplierRepository).should(times(1)).save(expectedResult);
    then(projectionFactory)
        .should(times(1))
        .createProjection(SupplierResponseDto.class, expectedResult);
  }

  @Test
  void findById() {
    final SupplierEntity expectedResult = getSupplierEntity();
    given(supplierRepository.findByNif(EXISTING_NIF)).willReturn(Optional.of(expectedResult));
    given(projectionFactory.createProjection(SupplierResponseDto.class, expectedResult))
        .willReturn(supplierResponseDto);

    final SupplierResponseDto actualResult = sut.findById(EXISTING_NIF);

    supplierAsserts(expectedResult, actualResult);
    then(supplierRepository).should(times(1)).findByNif(EXISTING_NIF);
    then(projectionFactory)
        .should(times(1))
        .createProjection(SupplierResponseDto.class, expectedResult);
  }

  @Test
  void delete() {
    final SupplierEntity expectedResult = getSupplierEntity();
    given(supplierRepository.findByNif(EXISTING_NIF)).willReturn(Optional.of(expectedResult));

    sut.delete(EXISTING_NIF);

    then(supplierRepository).should(times(1)).findByNif(EXISTING_NIF);
    then(supplierRepository).should(times(1)).delete(expectedResult);
  }

  @Test
  void update() {
    final SupplierEntity expectedResult = getSupplierEntity();
    given(supplierRepository.findByNif(EXISTING_NIF)).willReturn(Optional.of(expectedResult));
    given(supplierRepository.save(expectedResult)).willReturn(expectedResult);
    given(projectionFactory.createProjection(SupplierResponseDto.class, expectedResult))
        .willReturn(supplierResponseDto);

    final SupplierResponseDto actualResult = sut.update(EXISTING_NIF, supplierRequestDto);

    supplierAsserts(expectedResult, actualResult);
    then(supplierRepository).should(times(1)).findByNif(EXISTING_NIF);
    then(supplierRepository).should(times(1)).save(expectedResult);
    then(projectionFactory)
        .should(times(1))
        .createProjection(SupplierResponseDto.class, expectedResult);
  }

  private static void supplierAsserts(
      SupplierEntity expectedResult, SupplierResponseDto actualResult) {
    assertAll(
        () -> assertEquals(expectedResult.getName(), actualResult.getName()),
        () -> assertEquals(expectedResult.getNif(), actualResult.getNif()),
        () -> assertEquals(expectedResult.getEmail(), actualResult.getEmail()));
  }
}
