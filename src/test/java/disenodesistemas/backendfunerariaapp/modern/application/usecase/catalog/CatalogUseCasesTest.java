package disenodesistemas.backendfunerariaapp.modern.application.usecase.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.BrandPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.CategoryPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.CityPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.GenderPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ProvincePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptTypePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.RelationshipPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.city.CityQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.gender.GenderQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.province.ProvinceQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.relationship.RelationshipQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.role.RoleQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.BrandMapper;
import disenodesistemas.backendfunerariaapp.mapping.CategoryMapper;
import disenodesistemas.backendfunerariaapp.mapping.CityMapper;
import disenodesistemas.backendfunerariaapp.mapping.DeathCauseMapper;
import disenodesistemas.backendfunerariaapp.mapping.GenderMapper;
import disenodesistemas.backendfunerariaapp.mapping.ProvinceMapper;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.mapping.RelationshipMapper;
import disenodesistemas.backendfunerariaapp.mapping.RoleMapper;
import disenodesistemas.backendfunerariaapp.modern.support.DomainTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Catalog Use Cases")
class CatalogUseCasesTest {

  @Test
  @DisplayName(
      "Given a brand with related items when delete is requested then it rejects the delete as a conflict")
  void givenABrandWithRelatedItemsWhenDeleteIsRequestedThenItRejectsTheDeleteAsAConflict() {
    final BrandPersistencePort brandPersistencePort = mock(BrandPersistencePort.class);
    final BrandQueryUseCase brandQueryUseCase =
        new BrandQueryUseCase(brandPersistencePort, mock(BrandMapper.class));
    final BrandCommandUseCase brandCommandUseCase =
        new BrandCommandUseCase(brandPersistencePort, mock(BrandMapper.class), brandQueryUseCase);
    final BrandEntity brand = DomainTestDataFactory.brandEntity();
    brand.setBrandItems(List.of(new ItemEntity()));

    when(brandPersistencePort.findById(1L)).thenReturn(Optional.of(brand));

    assertThatThrownBy(() -> brandCommandUseCase.delete(1L))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("brand.error.invalid.delete");
  }

  @Test
  @DisplayName(
      "Given a persisted brand when findById is requested then it resolves the entity and maps the response")
  void givenAPersistedBrandWhenFindByIdIsRequestedThenItResolvesTheEntityAndMapsTheResponse() {
    final BrandPersistencePort brandPersistencePort = mock(BrandPersistencePort.class);
    final BrandMapper brandMapper = mock(BrandMapper.class);
    final BrandQueryUseCase brandQueryUseCase = new BrandQueryUseCase(brandPersistencePort, brandMapper);
    final BrandEntity brand = DomainTestDataFactory.brandEntity();
    final BrandResponseDto response =
        new BrandResponseDto(1L, TestValues.BRAND_NAME, TestValues.BRAND_WEB_PAGE);

    when(brandPersistencePort.findById(1L)).thenReturn(Optional.of(brand));
    when(brandMapper.toDto(brand)).thenReturn(response);

    assertThat(brandQueryUseCase.findById(1L)).isEqualTo(response);
  }

  @Test
  @DisplayName(
      "Given a category with related items when delete is requested then it rejects the delete as a conflict")
  void givenACategoryWithRelatedItemsWhenDeleteIsRequestedThenItRejectsTheDeleteAsAConflict() {
    final CategoryPersistencePort categoryPersistencePort = mock(CategoryPersistencePort.class);
    final CategoryQueryUseCase categoryQueryUseCase =
        new CategoryQueryUseCase(categoryPersistencePort, mock(CategoryMapper.class));
    final CategoryCommandUseCase categoryCommandUseCase =
        new CategoryCommandUseCase(
            categoryPersistencePort, mock(CategoryMapper.class), categoryQueryUseCase);
    final CategoryEntity category = DomainTestDataFactory.categoryEntity();
    category.setItems(List.of(new ItemEntity()));

    when(categoryPersistencePort.findById(1L)).thenReturn(Optional.of(category));

    assertThatThrownBy(() -> categoryCommandUseCase.delete(1L))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("category.error.invalid.delete");
  }

  @Test
  @DisplayName(
      "Given persisted categories when findAll is requested then it maps the ordered result")
  void givenPersistedCategoriesWhenFindAllIsRequestedThenItMapsTheOrderedResult() {
    final CategoryPersistencePort categoryPersistencePort = mock(CategoryPersistencePort.class);
    final CategoryMapper categoryMapper = mock(CategoryMapper.class);
    final CategoryQueryUseCase categoryQueryUseCase =
        new CategoryQueryUseCase(categoryPersistencePort, categoryMapper);
    final CategoryEntity category = DomainTestDataFactory.categoryEntity();
    final CategoryResponseDto response =
        new CategoryResponseDto(1L, TestValues.CATEGORY_NAME, TestValues.CATEGORY_DESCRIPTION);

    when(categoryPersistencePort.findAllByOrderByName()).thenReturn(List.of(category));
    when(categoryMapper.toDto(category)).thenReturn(response);

    assertThat(categoryQueryUseCase.findAll()).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given an existing death cause when it is updated then it changes the name and persists the aggregate")
  void givenAnExistingDeathCauseWhenItIsUpdatedThenItChangesTheNameAndPersistsTheAggregate() {
    final DeathCausePersistencePort deathCausePersistencePort = mock(DeathCausePersistencePort.class);
    final DeathCauseMapper deathCauseMapper = mock(DeathCauseMapper.class);
    final DeathCauseQueryUseCase deathCauseQueryUseCase =
        new DeathCauseQueryUseCase(deathCausePersistencePort, deathCauseMapper);
    final DeathCauseCommandUseCase deathCauseCommandUseCase =
        new DeathCauseCommandUseCase(deathCausePersistencePort, deathCauseMapper, deathCauseQueryUseCase);
    final DeathCauseEntity entity = DomainTestDataFactory.deathCauseEntity();
    final DeathCauseResponseDto response = new DeathCauseResponseDto(1L, "Accidental");

    when(deathCausePersistencePort.findById(1L)).thenReturn(Optional.of(entity));
    when(deathCausePersistencePort.save(entity)).thenReturn(entity);
    when(deathCauseMapper.toDto(entity)).thenReturn(response);

    final DeathCauseResponseDto updated =
        deathCauseCommandUseCase.update(1L, DeathCauseDto.builder().id(1L).name("Accidental").build());

    assertThat(updated).isEqualTo(response);
    assertThat(entity.getName()).isEqualTo("Accidental");
    verify(deathCausePersistencePort).save(entity);
  }

  @Test
  @DisplayName(
      "Given a missing death cause when findById is requested then it throws a not found exception")
  void givenAMissingDeathCauseWhenFindByIdIsRequestedThenItThrowsANotFoundException() {
    final DeathCausePersistencePort deathCausePersistencePort = mock(DeathCausePersistencePort.class);
    final DeathCauseQueryUseCase deathCauseQueryUseCase =
        new DeathCauseQueryUseCase(deathCausePersistencePort, mock(DeathCauseMapper.class));

    when(deathCausePersistencePort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deathCauseQueryUseCase.findById(99L))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("death.cause.not.found");
  }

  @Test
  @DisplayName(
      "Given cities for a province when they are requested then it maps the ordered city list")
  void givenCitiesForAProvinceWhenTheyAreRequestedThenItMapsTheOrderedCityList() {
    final CityPersistencePort cityPersistencePort = mock(CityPersistencePort.class);
    final CityMapper cityMapper = mock(CityMapper.class);
    final CityQueryUseCase cityQueryUseCase = new CityQueryUseCase(cityPersistencePort, cityMapper);
    final CityEntity city = DomainTestDataFactory.cityEntity();
    final CityResponseDto response = new CityResponseDto(1L, "Cordoba", "5000", null);

    when(cityPersistencePort.findByProvinceIdOrderByName(2L)).thenReturn(List.of(city));
    when(cityMapper.toDto(city)).thenReturn(response);

    assertThat(cityQueryUseCase.findByProvinceId(2L)).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given a missing gender when it is requested by id then it throws a not found exception")
  void givenAMissingGenderWhenItIsRequestedByIdThenItThrowsANotFoundException() {
    final GenderPersistencePort genderPersistencePort = mock(GenderPersistencePort.class);
    final GenderQueryUseCase genderQueryUseCase =
        new GenderQueryUseCase(genderPersistencePort, mock(GenderMapper.class));

    when(genderPersistencePort.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> genderQueryUseCase.getGenderById(5L))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("gender.error.not.found");
  }

  @Test
  @DisplayName(
      "Given provinces in storage when all provinces are requested then it maps the ordered result")
  void givenProvincesInStorageWhenAllProvincesAreRequestedThenItMapsTheOrderedResult() {
    final ProvincePersistencePort provincePersistencePort = mock(ProvincePersistencePort.class);
    final ProvinceMapper provinceMapper = mock(ProvinceMapper.class);
    final ProvinceQueryUseCase provinceQueryUseCase =
        new ProvinceQueryUseCase(provincePersistencePort, provinceMapper);
    final ProvinceEntity province = DomainTestDataFactory.provinceEntity();
    final ProvinceResponseDto response = new ProvinceResponseDto(1L, "Cordoba", "AR-X");

    when(provincePersistencePort.findAllByOrderByName()).thenReturn(List.of(province));
    when(provinceMapper.toDto(province)).thenReturn(response);

    assertThat(provinceQueryUseCase.getAllProvinces()).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given a missing receipt type name when it is searched then it throws a not found exception")
  void givenAMissingReceiptTypeNameWhenItIsSearchedThenItThrowsANotFoundException() {
    final ReceiptTypePersistencePort receiptTypePersistencePort = mock(ReceiptTypePersistencePort.class);
    final ReceiptTypeQueryUseCase receiptTypeQueryUseCase =
        new ReceiptTypeQueryUseCase(receiptTypePersistencePort, mock(ReceiptTypeMapper.class));

    when(receiptTypePersistencePort.findByNameIsContainingIgnoreCase("Factura X"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> receiptTypeQueryUseCase.findByNameIsContainingIgnoreCase("Factura X"))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("receiptType.error.name.not.found ");
  }

  @Test
  @DisplayName(
      "Given relationships in storage when they are requested then it maps the ordered result")
  void givenRelationshipsInStorageWhenTheyAreRequestedThenItMapsTheOrderedResult() {
    final RelationshipPersistencePort relationshipPersistencePort = mock(RelationshipPersistencePort.class);
    final RelationshipMapper relationshipMapper = mock(RelationshipMapper.class);
    final RelationshipQueryUseCase relationshipQueryUseCase =
        new RelationshipQueryUseCase(relationshipPersistencePort, relationshipMapper);
    final RelationshipEntity relationship = DomainTestDataFactory.relationshipEntity();
    final RelationshipResponseDto response = new RelationshipResponseDto(1L, "Padre");

    when(relationshipPersistencePort.findAllByOrderByName()).thenReturn(List.of(relationship));
    when(relationshipMapper.toDto(relationship)).thenReturn(response);

    assertThat(relationshipQueryUseCase.getRelationships()).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given roles in storage when they are requested then it maps the result into role DTOs")
  void givenRolesInStorageWhenTheyAreRequestedThenItMapsTheResultIntoRoleDtos() {
    final RolePersistencePort rolePersistencePort = mock(RolePersistencePort.class);
    final RoleMapper roleMapper = mock(RoleMapper.class);
    final RoleQueryUseCase roleQueryUseCase = new RoleQueryUseCase(rolePersistencePort, roleMapper);
    final RoleEntity roleEntity = DomainTestDataFactory.roleEntity(Role.ROLE_USER, 1L);
    final RolesDto response = new RolesDto(1L, "ROLE_USER");

    when(rolePersistencePort.findAll()).thenReturn(List.of(roleEntity));
    when(roleMapper.toDto(roleEntity)).thenReturn(response);

    assertThat(roleQueryUseCase.findAll()).containsExactly(response);
  }
}
