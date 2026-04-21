package disenodesistemas.backendfunerariaapp.modern.application.usecase.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.BrandPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.CategoryPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.CityPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ProvincePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptTypePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.RelationshipPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.SupplierPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.city.CityQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.province.ProvinceQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.relationship.RelationshipQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AddressMapper;
import disenodesistemas.backendfunerariaapp.mapping.BrandMapper;
import disenodesistemas.backendfunerariaapp.mapping.CategoryMapper;
import disenodesistemas.backendfunerariaapp.mapping.CityMapper;
import disenodesistemas.backendfunerariaapp.mapping.DeathCauseMapper;
import disenodesistemas.backendfunerariaapp.mapping.MobileNumberMapper;
import disenodesistemas.backendfunerariaapp.mapping.ProvinceMapper;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.mapping.RelationshipMapper;
import disenodesistemas.backendfunerariaapp.mapping.SupplierMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Catalog Additional Coverage")
class CatalogAdditionalCoverageTest {

  @Test
  @DisplayName(
      "Given persisted brands when the catalog queries are executed then the brand use cases map the list, resolve the entity and persist create-update commands")
  void givenPersistedBrandsWhenTheCatalogQueriesAreExecutedThenTheBrandUseCasesMapTheListResolveTheEntityAndPersistCreateUpdateCommands() {
    final BrandPersistencePort brandPersistencePort = org.mockito.Mockito.mock(BrandPersistencePort.class);
    final BrandMapper brandMapper = org.mockito.Mockito.mock(BrandMapper.class);
    final BrandQueryUseCase brandQueryUseCase = new BrandQueryUseCase(brandPersistencePort, brandMapper);
    final BrandCommandUseCase brandCommandUseCase =
        new BrandCommandUseCase(brandPersistencePort, brandMapper, brandQueryUseCase);
    final BrandRequestDto request = BrandRequestDto.builder().name("Acme").webPage("https://acme.example").build();
    final BrandEntity createdEntity = new BrandEntity("Acme", "https://acme.example");
    final BrandEntity persistedEntity = new BrandEntity("Acme", "https://acme.example");
    persistedEntity.setId(1L);
    final BrandResponseDto response = new BrandResponseDto(1L, "Acme", "https://acme.example");

    when(brandPersistencePort.findAllByOrderByName()).thenReturn(List.of(persistedEntity));
    when(brandPersistencePort.findById(1L)).thenReturn(Optional.of(persistedEntity));
    when(brandMapper.toEntity(request)).thenReturn(createdEntity);
    when(brandPersistencePort.save(createdEntity)).thenReturn(persistedEntity);
    when(brandPersistencePort.save(persistedEntity)).thenReturn(persistedEntity);
    when(brandMapper.toDto(persistedEntity)).thenReturn(response);

    assertThat(brandQueryUseCase.findAll()).containsExactly(response);
    assertThat(brandCommandUseCase.create(request)).isEqualTo(response);
    assertThat(brandCommandUseCase.update(1L, request)).isEqualTo(response);
    verify(brandMapper).updateEntity(request, persistedEntity);
  }

  @Test
  @DisplayName(
      "Given a missing brand identifier when the entity is requested then the query use case throws not found")
  void givenAMissingBrandIdentifierWhenTheEntityIsRequestedThenTheQueryUseCaseThrowsNotFound() {
    final BrandPersistencePort brandPersistencePort = org.mockito.Mockito.mock(BrandPersistencePort.class);
    final BrandQueryUseCase brandQueryUseCase =
        new BrandQueryUseCase(brandPersistencePort, org.mockito.Mockito.mock(BrandMapper.class));

    when(brandPersistencePort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> brandQueryUseCase.getBrandById(99L))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("brand.error.not.found");
  }

  @Test
  @DisplayName(
      "Given persisted categories when the category queries are executed then the use cases map the response and persist create-update commands")
  void givenPersistedCategoriesWhenTheCategoryQueriesAreExecutedThenTheUseCasesMapTheResponseAndPersistCreateUpdateCommands() {
    final CategoryPersistencePort categoryPersistencePort =
        org.mockito.Mockito.mock(CategoryPersistencePort.class);
    final CategoryMapper categoryMapper = org.mockito.Mockito.mock(CategoryMapper.class);
    final CategoryQueryUseCase categoryQueryUseCase =
        new CategoryQueryUseCase(categoryPersistencePort, categoryMapper);
    final CategoryCommandUseCase categoryCommandUseCase =
        new CategoryCommandUseCase(categoryPersistencePort, categoryMapper, categoryQueryUseCase);
    final CategoryRequestDto request =
        CategoryRequestDto.builder().name("Urnas").description("Productos funerarios").build();
    final CategoryEntity createdEntity = new CategoryEntity("Urnas", "Productos funerarios");
    final CategoryEntity persistedEntity = new CategoryEntity("Urnas", "Productos funerarios");
    persistedEntity.setId(1L);
    final CategoryResponseDto response =
        new CategoryResponseDto(1L, "Urnas", "Productos funerarios");

    when(categoryPersistencePort.findById(1L)).thenReturn(Optional.of(persistedEntity));
    when(categoryMapper.toEntity(request)).thenReturn(createdEntity);
    when(categoryPersistencePort.save(createdEntity)).thenReturn(persistedEntity);
    when(categoryPersistencePort.save(persistedEntity)).thenReturn(persistedEntity);
    when(categoryMapper.toDto(persistedEntity)).thenReturn(response);

    assertThat(categoryQueryUseCase.findById(1L)).isEqualTo(response);
    assertThat(categoryCommandUseCase.create(request)).isEqualTo(response);
    assertThat(categoryCommandUseCase.update(1L, request)).isEqualTo(response);
    verify(categoryMapper).updateEntity(request, persistedEntity);
  }

  @Test
  @DisplayName(
      "Given persisted suppliers when the supplier queries and commands are executed then the use cases resolve the entity, map the list and keep child collections synchronized")
  void givenPersistedSuppliersWhenTheSupplierQueriesAndCommandsAreExecutedThenTheUseCasesResolveTheEntityMapTheListAndKeepChildCollectionsSynchronized() {
    final SupplierPersistencePort supplierPersistencePort =
        org.mockito.Mockito.mock(SupplierPersistencePort.class);
    final SupplierMapper supplierMapper = org.mockito.Mockito.mock(SupplierMapper.class);
    final SupplierQueryUseCase supplierQueryUseCase =
        new SupplierQueryUseCase(supplierPersistencePort, supplierMapper);
    final SupplierCommandUseCase supplierCommandUseCase =
        new SupplierCommandUseCase(
            supplierPersistencePort,
            supplierMapper,
            org.mockito.Mockito.mock(MobileNumberMapper.class),
            org.mockito.Mockito.mock(AddressMapper.class),
            supplierQueryUseCase);
    final SupplierRequestDto request =
        SupplierRequestDto.builder()
            .name("Proveedor Uno")
            .nif("20-12345678-9")
            .email("proveedor@example.com")
            .build();
    final SupplierEntity createdEntity =
        new SupplierEntity("Proveedor Uno", "20-12345678-9", null, "proveedor@example.com");
    final SupplierEntity persistedEntity =
        new SupplierEntity("Proveedor Uno", "20-12345678-9", null, "proveedor@example.com");
    final SupplierResponseDto response =
        new SupplierResponseDto(
            "Proveedor Uno", "20-12345678-9", null, "proveedor@example.com", List.of(), List.of());

    when(supplierPersistencePort.findAllByOrderByIdDesc()).thenReturn(List.of(persistedEntity));
    when(supplierPersistencePort.findByNif("20-12345678-9")).thenReturn(Optional.of(persistedEntity));
    when(supplierMapper.toEntity(request)).thenReturn(createdEntity);
    when(supplierPersistencePort.save(createdEntity)).thenReturn(persistedEntity);
    when(supplierPersistencePort.save(persistedEntity)).thenReturn(persistedEntity);
    when(supplierMapper.toDto(persistedEntity)).thenReturn(response);

    assertThat(supplierQueryUseCase.findAll()).containsExactly(response);
    assertThat(supplierQueryUseCase.findById("20-12345678-9")).isEqualTo(response);
    assertThat(supplierCommandUseCase.create(request)).isEqualTo(response);
    assertThat(supplierCommandUseCase.update("20-12345678-9", request)).isEqualTo(response);
    supplierCommandUseCase.delete("20-12345678-9");

    verify(supplierMapper).updateEntity(request, persistedEntity);
    verify(supplierPersistencePort).delete(persistedEntity);
  }

  @Test
  @DisplayName(
      "Given a missing supplier nif when the supplier is requested then the query use case throws the application not found error")
  void givenAMissingSupplierNifWhenTheSupplierIsRequestedThenTheQueryUseCaseThrowsTheApplicationNotFoundError() {
    final SupplierPersistencePort supplierPersistencePort =
        org.mockito.Mockito.mock(SupplierPersistencePort.class);
    final SupplierQueryUseCase supplierQueryUseCase =
        new SupplierQueryUseCase(supplierPersistencePort, org.mockito.Mockito.mock(SupplierMapper.class));

    when(supplierPersistencePort.findByNif("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> supplierQueryUseCase.findSupplierEntityByNif("missing"))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("supplier.error.not.found");
  }

  @Test
  @DisplayName(
      "Given persisted death causes when create-update-delete and findAll are executed then the use cases map and persist the aggregate changes")
  void givenPersistedDeathCausesWhenCreateUpdateDeleteAndFindAllAreExecutedThenTheUseCasesMapAndPersistTheAggregateChanges() {
    final DeathCausePersistencePort deathCausePersistencePort =
        org.mockito.Mockito.mock(DeathCausePersistencePort.class);
    final DeathCauseMapper deathCauseMapper = org.mockito.Mockito.mock(DeathCauseMapper.class);
    final DeathCauseQueryUseCase deathCauseQueryUseCase =
        new DeathCauseQueryUseCase(deathCausePersistencePort, deathCauseMapper);
    final DeathCauseCommandUseCase deathCauseCommandUseCase =
        new DeathCauseCommandUseCase(deathCausePersistencePort, deathCauseMapper, deathCauseQueryUseCase);
    final DeathCauseEntity entity = new DeathCauseEntity("Natural");
    final DeathCauseResponseDto response = new DeathCauseResponseDto(1L, "Natural");

    when(deathCausePersistencePort.findAllByOrderByNameAsc()).thenReturn(List.of(entity));
    when(deathCausePersistencePort.findById(1L)).thenReturn(Optional.of(entity));
    when(deathCausePersistencePort.save(any(DeathCauseEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(deathCauseMapper.toDto(any(DeathCauseEntity.class))).thenReturn(response);

    assertThat(deathCauseQueryUseCase.findAll()).containsExactly(response);
    assertThat(deathCauseCommandUseCase.create(DeathCauseDto.builder().name("Natural").build()))
        .isEqualTo(response);
    assertThat(deathCauseCommandUseCase.update(1L, DeathCauseDto.builder().name("Natural").build()))
        .isEqualTo(response);
    deathCauseCommandUseCase.delete(1L);

    verify(deathCausePersistencePort).delete(entity);
  }

  @Test
  @DisplayName(
      "Given catalog reference data when city province receipt type and relationship queries are executed then the use cases map the expected responses")
  void givenCatalogReferenceDataWhenCityProvinceReceiptTypeAndRelationshipQueriesAreExecutedThenTheUseCasesMapTheExpectedResponses() {
    final CityPersistencePort cityPersistencePort = org.mockito.Mockito.mock(CityPersistencePort.class);
    final ProvincePersistencePort provincePersistencePort =
        org.mockito.Mockito.mock(ProvincePersistencePort.class);
    final ReceiptTypePersistencePort receiptTypePersistencePort =
        org.mockito.Mockito.mock(ReceiptTypePersistencePort.class);
    final RelationshipPersistencePort relationshipPersistencePort =
        org.mockito.Mockito.mock(RelationshipPersistencePort.class);
    final CityMapper cityMapper = org.mockito.Mockito.mock(CityMapper.class);
    final ProvinceMapper provinceMapper = org.mockito.Mockito.mock(ProvinceMapper.class);
    final ReceiptTypeMapper receiptTypeMapper = org.mockito.Mockito.mock(ReceiptTypeMapper.class);
    final RelationshipMapper relationshipMapper = org.mockito.Mockito.mock(RelationshipMapper.class);

    final CityQueryUseCase cityQueryUseCase = new CityQueryUseCase(cityPersistencePort, cityMapper);
    final ProvinceQueryUseCase provinceQueryUseCase =
        new ProvinceQueryUseCase(provincePersistencePort, provinceMapper);
    final ReceiptTypeQueryUseCase receiptTypeQueryUseCase =
        new ReceiptTypeQueryUseCase(receiptTypePersistencePort, receiptTypeMapper);
    final RelationshipQueryUseCase relationshipQueryUseCase =
        new RelationshipQueryUseCase(relationshipPersistencePort, relationshipMapper);

    final CityEntity city = CityEntity.builder().id(1L).name("Cordoba").zipCode("5000").build();
    final ProvinceEntity province = ProvinceEntity.builder().id(1L).name("Cordoba").code31662("AR-X").build();
    final ReceiptTypeEntity receiptType = new ReceiptTypeEntity("Factura A");
    final RelationshipEntity relationship = new RelationshipEntity("Padre");
    final CityResponseDto cityResponse = new CityResponseDto(1L, "Cordoba", "5000", null);
    final ProvinceResponseDto provinceResponse = new ProvinceResponseDto(1L, "Cordoba", "AR-X");
    final ReceiptTypeResponseDto receiptTypeResponse = new ReceiptTypeResponseDto(1L, "Factura A");
    final RelationshipResponseDto relationshipResponse = new RelationshipResponseDto(1L, "Padre");

    when(cityPersistencePort.findById(1L)).thenReturn(Optional.of(city));
    when(cityMapper.toDto(city)).thenReturn(cityResponse);
    when(provincePersistencePort.findById(1L)).thenReturn(Optional.of(province));
    when(provincePersistencePort.findAllByOrderByName()).thenReturn(List.of(province));
    when(provinceMapper.toDto(province)).thenReturn(provinceResponse);
    when(receiptTypePersistencePort.findAllByOrderByName()).thenReturn(List.of(receiptType));
    when(receiptTypeMapper.toDto(receiptType)).thenReturn(receiptTypeResponse);
    when(relationshipPersistencePort.findById(1L)).thenReturn(Optional.of(relationship));
    when(relationshipMapper.toDto(relationship)).thenReturn(relationshipResponse);

    assertThat(cityQueryUseCase.findById(1L)).isEqualTo(cityResponse);
    assertThat(provinceQueryUseCase.getProvinceById(1L)).isEqualTo(province);
    assertThat(provinceQueryUseCase.getAllProvinces()).containsExactly(provinceResponse);
    assertThat(receiptTypeQueryUseCase.getAllReceiptTypes()).containsExactly(receiptTypeResponse);
    assertThat(relationshipQueryUseCase.getRelationshipById(1L)).isEqualTo(relationship);
  }
}
