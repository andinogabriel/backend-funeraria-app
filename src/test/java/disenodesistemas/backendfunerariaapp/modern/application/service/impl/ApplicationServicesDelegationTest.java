package disenodesistemas.backendfunerariaapp.modern.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.service.impl.AffiliateServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.BrandServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.CategoryServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.CityServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.DeathCauseServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.DeceasedServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.GenderServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.ItemServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.ProvinceServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.ReceiptTypeServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.RelationshipServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.RoleServiceImpl;
import disenodesistemas.backendfunerariaapp.application.service.impl.SupplierServiceImpl;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.city.CityQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.gender.GenderQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.province.ProvinceQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.relationship.RelationshipQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.role.RoleQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.web.dto.RolesDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ReceiptTypeResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.RelationshipResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Application Services Delegation")
class ApplicationServicesDelegationTest {

  @Test
  @DisplayName(
      "Given affiliate commands and queries when the affiliate service is invoked then it delegates each operation to the corresponding use case")
  void givenAffiliateCommandsAndQueriesWhenTheAffiliateServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final AffiliateCommandUseCase command = mock(AffiliateCommandUseCase.class);
    final AffiliateQueryUseCase query = mock(AffiliateQueryUseCase.class);
    final AffiliateServiceImpl service = new AffiliateServiceImpl(command, query);
    final AffiliateRequestDto request = AffiliateRequestDto.builder().build();
    final AffiliateResponseDto response = new AffiliateResponseDto(null, null, null, null, null, null, null, null, null, List.of(), List.of());

    when(command.create(request)).thenReturn(response);
    when(query.findAll()).thenReturn(List.of(response));

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.findAll()).containsExactly(response);
    service.delete(1);
    verify(command).create(request);
    verify(query).findAll();
    verify(command).delete(1);
  }

  @Test
  @DisplayName(
      "Given brand commands and queries when the brand service is invoked then it delegates each operation to the corresponding use case")
  void givenBrandCommandsAndQueriesWhenTheBrandServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final BrandCommandUseCase command = mock(BrandCommandUseCase.class);
    final BrandQueryUseCase query = mock(BrandQueryUseCase.class);
    final BrandServiceImpl service = new BrandServiceImpl(command, query);
    final BrandRequestDto request = BrandRequestDto.builder().name("Acme").build();
    final BrandResponseDto response = new BrandResponseDto(1L, "Acme", "https://acme.example");
    final BrandEntity entity = new BrandEntity("Acme", "https://acme.example");

    when(command.create(request)).thenReturn(response);
    when(query.getBrandById(1L)).thenReturn(entity);

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.getBrandById(1L)).isEqualTo(entity);
    verify(command).create(request);
    verify(query).getBrandById(1L);
  }

  @Test
  @DisplayName(
      "Given category commands and queries when the category service is invoked then it delegates each operation to the corresponding use case")
  void givenCategoryCommandsAndQueriesWhenTheCategoryServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final CategoryCommandUseCase command = mock(CategoryCommandUseCase.class);
    final CategoryQueryUseCase query = mock(CategoryQueryUseCase.class);
    final CategoryServiceImpl service = new CategoryServiceImpl(command, query);
    final CategoryRequestDto request = CategoryRequestDto.builder().name("Urnas").build();
    final CategoryResponseDto response = new CategoryResponseDto(1L, "Urnas", "Productos");
    final CategoryEntity entity = new CategoryEntity("Urnas", "Productos");

    when(command.create(request)).thenReturn(response);
    when(query.findCategoryEntityById(1L)).thenReturn(entity);

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.findCategoryEntityById(1L)).isEqualTo(entity);
    verify(command).create(request);
    verify(query).findCategoryEntityById(1L);
  }

  @Test
  @DisplayName(
      "Given city queries when the city service is invoked then it delegates the reads to the query use case")
  void givenCityQueriesWhenTheCityServiceIsInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final CityQueryUseCase query = mock(CityQueryUseCase.class);
    final CityServiceImpl service = new CityServiceImpl(query);
    final CityResponseDto response = new CityResponseDto(1L, "Cordoba", "5000", null);

    when(query.findById(1L)).thenReturn(response);

    assertThat(service.findById(1L)).isEqualTo(response);
    verify(query).findById(1L);
  }

  @Test
  @DisplayName(
      "Given death cause commands and queries when the death cause service is invoked then it delegates each operation to the corresponding use case")
  void givenDeathCauseCommandsAndQueriesWhenTheDeathCauseServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final DeathCauseCommandUseCase command = mock(DeathCauseCommandUseCase.class);
    final DeathCauseQueryUseCase query = mock(DeathCauseQueryUseCase.class);
    final DeathCauseServiceImpl service = new DeathCauseServiceImpl(command, query);
    final DeathCauseDto request = DeathCauseDto.builder().name("Natural").build();
    final DeathCauseResponseDto response = new DeathCauseResponseDto(1L, "Natural");

    when(command.create(request)).thenReturn(response);
    when(query.findAll()).thenReturn(List.of(response));

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.findAll()).containsExactly(response);
    verify(command).create(request);
    verify(query).findAll();
  }

  @Test
  @DisplayName(
      "Given deceased commands and queries when the deceased service is invoked then it delegates each operation to the corresponding use case")
  void givenDeceasedCommandsAndQueriesWhenTheDeceasedServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final DeceasedCommandUseCase command = mock(DeceasedCommandUseCase.class);
    final DeceasedQueryUseCase query = mock(DeceasedQueryUseCase.class);
    final DeceasedServiceImpl service = new DeceasedServiceImpl(command, query);
    final DeceasedRequestDto request = DeceasedRequestDto.builder().build();
    final DeceasedResponseDto response =
        new DeceasedResponseDto(1L, null, null, 30111222, false, null, null, null, null, null, null, null, null);

    when(command.create(request)).thenReturn(response);
    when(query.findById(30111222)).thenReturn(response);

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.findById(30111222)).isEqualTo(response);
    verify(command).create(request);
    verify(query).findById(30111222);
  }

  @Test
  @DisplayName(
      "Given gender queries when the gender service is invoked then it delegates the reads to the query use case")
  void givenGenderQueriesWhenTheGenderServiceIsInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final GenderQueryUseCase query = mock(GenderQueryUseCase.class);
    final GenderServiceImpl service = new GenderServiceImpl(query);
    final GenderEntity entity = new GenderEntity("Masculino");
    final GenderResponseDto response = new GenderResponseDto(1L, "Masculino");

    when(query.getGenderById(1L)).thenReturn(entity);
    when(query.getGenders()).thenReturn(List.of(response));

    assertThat(service.getGenderById(1L)).isEqualTo(entity);
    assertThat(service.getGenders()).containsExactly(response);
    verify(query).getGenderById(1L);
    verify(query).getGenders();
  }

  @Test
  @DisplayName(
      "Given item commands and queries when the item service is invoked then it delegates each operation to the corresponding use case")
  void givenItemCommandsAndQueriesWhenTheItemServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final ItemCommandUseCase command = mock(ItemCommandUseCase.class);
    final ItemQueryUseCase query = mock(ItemQueryUseCase.class);
    final ItemServiceImpl service = new ItemServiceImpl(command, query);
    final ItemRequestDto request = ItemRequestDto.builder().name("Urna").build();
    final FilePayload filePayload = new FilePayload("image.png", "image/png", new byte[] {1});
    final ItemResponseDto response =
        new ItemResponseDto("Urna", null, "ITEM-001", null, null, null, null, null, null, null, null);

    when(command.create(request)).thenReturn(response);
    when(query.findById("ITEM-001")).thenReturn(response);

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.findById("ITEM-001")).isEqualTo(response);
    service.uploadItemImage("ITEM-001", filePayload);
    verify(command).create(request);
    verify(query).findById("ITEM-001");
    verify(command).uploadItemImage("ITEM-001", filePayload);
  }

  @Test
  @DisplayName(
      "Given province queries when the province service is invoked then it delegates the reads to the query use case")
  void givenProvinceQueriesWhenTheProvinceServiceIsInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final ProvinceQueryUseCase query = mock(ProvinceQueryUseCase.class);
    final ProvinceServiceImpl service = new ProvinceServiceImpl(query);
    final ProvinceEntity entity = ProvinceEntity.builder().id(1L).name("Cordoba").code31662("AR-X").build();
    final ProvinceResponseDto response = new ProvinceResponseDto(1L, "Cordoba", "AR-X");

    when(query.getProvinceById(1L)).thenReturn(entity);
    when(query.getAllProvinces()).thenReturn(List.of(response));

    assertThat(service.getProvinceById(1L)).isEqualTo(entity);
    assertThat(service.getAllProvinces()).containsExactly(response);
    verify(query).getProvinceById(1L);
    verify(query).getAllProvinces();
  }

  @Test
  @DisplayName(
      "Given receipt type queries when the receipt type service is invoked then it delegates the reads to the query use case")
  void givenReceiptTypeQueriesWhenTheReceiptTypeServiceIsInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final ReceiptTypeQueryUseCase query = mock(ReceiptTypeQueryUseCase.class);
    final ReceiptTypeServiceImpl service = new ReceiptTypeServiceImpl(query);
    final ReceiptTypeEntity entity = new ReceiptTypeEntity("Factura A");
    final ReceiptTypeResponseDto response = new ReceiptTypeResponseDto(1L, "Factura A");

    when(query.findByNameIsContainingIgnoreCase("Factura A")).thenReturn(entity);
    when(query.getAllReceiptTypes()).thenReturn(List.of(response));

    assertThat(service.findByNameIsContainingIgnoreCase("Factura A")).isEqualTo(entity);
    assertThat(service.getAllReceiptTypes()).containsExactly(response);
    verify(query).findByNameIsContainingIgnoreCase("Factura A");
    verify(query).getAllReceiptTypes();
  }

  @Test
  @DisplayName(
      "Given relationship queries when the relationship service is invoked then it delegates the reads to the query use case")
  void givenRelationshipQueriesWhenTheRelationshipServiceIsInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final RelationshipQueryUseCase query = mock(RelationshipQueryUseCase.class);
    final RelationshipServiceImpl service = new RelationshipServiceImpl(query);
    final RelationshipEntity entity = new RelationshipEntity("Padre");
    final RelationshipResponseDto response = new RelationshipResponseDto(1L, "Padre");

    when(query.getRelationshipById(1L)).thenReturn(entity);
    when(query.getRelationships()).thenReturn(List.of(response));

    assertThat(service.getRelationshipById(1L)).isEqualTo(entity);
    assertThat(service.getRelationships()).containsExactly(response);
    verify(query).getRelationshipById(1L);
    verify(query).getRelationships();
  }

  @Test
  @DisplayName(
      "Given role queries when the role service is invoked then it delegates the reads to the query use case")
  void givenRoleQueriesWhenTheRoleServiceIsInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final RoleQueryUseCase query = mock(RoleQueryUseCase.class);
    final RoleServiceImpl service = new RoleServiceImpl(query);
    final RolesDto response = new RolesDto(1L, "ROLE_USER");

    when(query.findAll()).thenReturn(List.of(response));

    assertThat(service.findAll()).containsExactly(response);
    verify(query).findAll();
  }

  @Test
  @DisplayName(
      "Given supplier commands and queries when the supplier service is invoked then it delegates each operation to the corresponding use case")
  void givenSupplierCommandsAndQueriesWhenTheSupplierServiceIsInvokedThenItDelegatesEachOperationToTheCorrespondingUseCase() {
    final SupplierCommandUseCase command = mock(SupplierCommandUseCase.class);
    final SupplierQueryUseCase query = mock(SupplierQueryUseCase.class);
    final SupplierServiceImpl service = new SupplierServiceImpl(command, query);
    final SupplierRequestDto request =
        SupplierRequestDto.builder().name("Proveedor Uno").nif("20-12345678-9").email("proveedor@example.com").build();
    final SupplierResponseDto response =
        new SupplierResponseDto("Proveedor Uno", "20-12345678-9", null, "proveedor@example.com", List.of(), List.of());
    final SupplierEntity entity =
        new SupplierEntity("Proveedor Uno", "20-12345678-9", null, "proveedor@example.com");

    when(command.create(request)).thenReturn(response);
    when(query.findSupplierEntityByNif("20-12345678-9")).thenReturn(entity);

    assertThat(service.create(request)).isEqualTo(response);
    assertThat(service.findSupplierEntityByNif("20-12345678-9")).isEqualTo(entity);
    verify(command).create(request);
    verify(query).findSupplierEntityByNif("20-12345678-9");
  }
}
