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
import disenodesistemas.backendfunerariaapp.application.service.impl.ItemServiceImpl;
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
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
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
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Application Services Additional Delegation")
class ApplicationServicesAdditionalDelegationTest {

  @Test
  @DisplayName(
      "Given affiliate service operations not covered by the primary suite when they are invoked then the service delegates each one to the matching use case")
  void givenAffiliateServiceOperationsNotCoveredByThePrimarySuiteWhenTheyAreInvokedThenTheServiceDelegatesEachOneToTheMatchingUseCase() {
    final AffiliateCommandUseCase command = mock(AffiliateCommandUseCase.class);
    final AffiliateQueryUseCase query = mock(AffiliateQueryUseCase.class);
    final AffiliateServiceImpl service = new AffiliateServiceImpl(command, query);
    final AffiliateRequestDto request = AffiliateRequestDto.builder().dni(30111222).build();
    final AffiliateResponseDto response =
        new AffiliateResponseDto(null, null, 30111222, null, null, Boolean.FALSE, null, null, null, List.of(), List.of());

    when(command.update(30111222, request)).thenReturn(response);
    when(query.findAllByDeceasedFalse()).thenReturn(List.of(response));
    when(query.findById(30111222)).thenReturn(response);
    when(query.findAffiliatesByUser()).thenReturn(List.of(response));
    when(query.findAffiliatesByFirstNameOrLastNameOrDniContaining("juan")).thenReturn(List.of(response));

    assertThat(service.update(30111222, request)).isEqualTo(response);
    assertThat(service.findAllByDeceasedFalse()).containsExactly(response);
    assertThat(service.findById(30111222)).isEqualTo(response);
    assertThat(service.findAffiliatesByUser()).containsExactly(response);
    assertThat(service.findAffiliatesByFirstNameOrLastNameOrDniContaining("juan")).containsExactly(response);
    verify(command).update(30111222, request);
    verify(query).findAllByDeceasedFalse();
    verify(query).findById(30111222);
    verify(query).findAffiliatesByUser();
    verify(query).findAffiliatesByFirstNameOrLastNameOrDniContaining("juan");
  }

  @Test
  @DisplayName(
      "Given remaining brand and category service operations when they are invoked then each service delegates the call to the appropriate command or query use case")
  void givenRemainingBrandAndCategoryServiceOperationsWhenTheyAreInvokedThenEachServiceDelegatesTheCallToTheAppropriateCommandOrQueryUseCase() {
    final BrandCommandUseCase brandCommand = mock(BrandCommandUseCase.class);
    final BrandQueryUseCase brandQuery = mock(BrandQueryUseCase.class);
    final BrandServiceImpl brandService = new BrandServiceImpl(brandCommand, brandQuery);
    final CategoryCommandUseCase categoryCommand = mock(CategoryCommandUseCase.class);
    final CategoryQueryUseCase categoryQuery = mock(CategoryQueryUseCase.class);
    final CategoryServiceImpl categoryService = new CategoryServiceImpl(categoryCommand, categoryQuery);
    final BrandRequestDto brandRequest = BrandRequestDto.builder().name("Acme").build();
    final CategoryRequestDto categoryRequest = CategoryRequestDto.builder().name("Urnas").build();
    final BrandResponseDto brandResponse = new BrandResponseDto(1L, "Acme", null);
    final CategoryResponseDto categoryResponse = new CategoryResponseDto(1L, "Urnas", null);

    when(brandQuery.findAll()).thenReturn(List.of(brandResponse));
    when(brandQuery.findById(1L)).thenReturn(brandResponse);
    when(brandCommand.update(1L, brandRequest)).thenReturn(brandResponse);
    when(categoryQuery.findAll()).thenReturn(List.of(categoryResponse));
    when(categoryQuery.findById(1L)).thenReturn(categoryResponse);
    when(categoryCommand.update(1L, categoryRequest)).thenReturn(categoryResponse);

    assertThat(brandService.findAll()).containsExactly(brandResponse);
    assertThat(brandService.findById(1L)).isEqualTo(brandResponse);
    assertThat(brandService.update(1L, brandRequest)).isEqualTo(brandResponse);
    brandService.delete(1L);
    assertThat(categoryService.findAll()).containsExactly(categoryResponse);
    assertThat(categoryService.findById(1L)).isEqualTo(categoryResponse);
    assertThat(categoryService.update(1L, categoryRequest)).isEqualTo(categoryResponse);
    categoryService.delete(1L);

    verify(brandQuery).findAll();
    verify(brandQuery).findById(1L);
    verify(brandCommand).update(1L, brandRequest);
    verify(brandCommand).delete(1L);
    verify(categoryQuery).findAll();
    verify(categoryQuery).findById(1L);
    verify(categoryCommand).update(1L, categoryRequest);
    verify(categoryCommand).delete(1L);
  }

  @Test
  @DisplayName(
      "Given remaining city death cause and deceased service operations when they are invoked then the services delegate every call to their use cases")
  void givenRemainingCityDeathCauseAndDeceasedServiceOperationsWhenTheyAreInvokedThenTheServicesDelegateEveryCallToTheirUseCases() {
    final CityQueryUseCase cityQuery = mock(CityQueryUseCase.class);
    final CityServiceImpl cityService = new CityServiceImpl(cityQuery);
    final DeathCauseCommandUseCase deathCauseCommand = mock(DeathCauseCommandUseCase.class);
    final DeathCauseQueryUseCase deathCauseQuery = mock(DeathCauseQueryUseCase.class);
    final DeathCauseServiceImpl deathCauseService = new DeathCauseServiceImpl(deathCauseCommand, deathCauseQuery);
    final DeceasedCommandUseCase deceasedCommand = mock(DeceasedCommandUseCase.class);
    final DeceasedQueryUseCase deceasedQuery = mock(DeceasedQueryUseCase.class);
    final DeceasedServiceImpl deceasedService = new DeceasedServiceImpl(deceasedCommand, deceasedQuery);
    final DeathCauseDto deathCauseRequest = DeathCauseDto.builder().name("Natural").build();
    final DeceasedRequestDto deceasedRequest = DeceasedRequestDto.builder().dni(30111222).build();
    final CityResponseDto cityResponse = new CityResponseDto(1L, "Cordoba", "5000", null);
    final DeathCauseResponseDto deathCauseResponse = new DeathCauseResponseDto(1L, "Natural");
    final DeceasedResponseDto deceasedResponse =
        new DeceasedResponseDto(1L, null, null, 30111222, false, null, null, null, null, null, null, null, null);

    when(cityQuery.findByProvinceId(1L)).thenReturn(List.of(cityResponse));
    when(deathCauseCommand.update(1L, deathCauseRequest)).thenReturn(deathCauseResponse);
    when(deathCauseQuery.findById(1L)).thenReturn(deathCauseResponse);
    when(deceasedQuery.findAll()).thenReturn(List.of(deceasedResponse));
    when(deceasedCommand.update(30111222, deceasedRequest)).thenReturn(deceasedResponse);

    assertThat(cityService.findByProvinceId(1L)).containsExactly(cityResponse);
    assertThat(deathCauseService.update(1L, deathCauseRequest)).isEqualTo(deathCauseResponse);
    assertThat(deathCauseService.findById(1L)).isEqualTo(deathCauseResponse);
    deathCauseService.delete(1L);
    assertThat(deceasedService.findAll()).containsExactly(deceasedResponse);
    assertThat(deceasedService.update(30111222, deceasedRequest)).isEqualTo(deceasedResponse);
    deceasedService.delete(30111222);

    verify(cityQuery).findByProvinceId(1L);
    verify(deathCauseCommand).update(1L, deathCauseRequest);
    verify(deathCauseQuery).findById(1L);
    verify(deathCauseCommand).delete(1L);
    verify(deceasedQuery).findAll();
    verify(deceasedCommand).update(30111222, deceasedRequest);
    verify(deceasedCommand).delete(30111222);
  }

  @Test
  @DisplayName(
      "Given remaining item and supplier service operations when they are invoked then the services delegate them to the corresponding use case")
  void givenRemainingItemAndSupplierServiceOperationsWhenTheyAreInvokedThenTheServicesDelegateThemToTheCorrespondingUseCase() {
    final ItemCommandUseCase itemCommand = mock(ItemCommandUseCase.class);
    final ItemQueryUseCase itemQuery = mock(ItemQueryUseCase.class);
    final ItemServiceImpl itemService = new ItemServiceImpl(itemCommand, itemQuery);
    final SupplierCommandUseCase supplierCommand = mock(SupplierCommandUseCase.class);
    final SupplierQueryUseCase supplierQuery = mock(SupplierQueryUseCase.class);
    final SupplierServiceImpl supplierService = new SupplierServiceImpl(supplierCommand, supplierQuery);
    final ItemRequestDto itemRequest = ItemRequestDto.builder().name("Urna").build();
    final FilePayload filePayload = new FilePayload("image.png", "image/png", new byte[] {1, 2, 3});
    final SupplierRequestDto supplierRequest =
        SupplierRequestDto.builder().name("Proveedor Uno").nif("20-12345678-9").email("proveedor@example.com").build();
    final ItemResponseDto itemResponse =
        new ItemResponseDto("Urna", null, "ITEM-001", null, null, null, null, null, null, null, null);
    final SupplierResponseDto supplierResponse =
        new SupplierResponseDto("Proveedor Uno", "20-12345678-9", null, "proveedor@example.com", List.of(), List.of());

    when(itemQuery.findAll()).thenReturn(List.of(itemResponse));
    when(itemQuery.getItemsByCategoryId(1L)).thenReturn(List.of(itemResponse));
    when(itemCommand.update("ITEM-001", itemRequest)).thenReturn(itemResponse);
    when(supplierQuery.findAll()).thenReturn(List.of(supplierResponse));
    when(supplierQuery.findById("20-12345678-9")).thenReturn(supplierResponse);
    when(supplierCommand.update("20-12345678-9", supplierRequest)).thenReturn(supplierResponse);

    assertThat(itemService.findAll()).containsExactly(itemResponse);
    assertThat(itemService.getItemsByCategoryId(1L)).containsExactly(itemResponse);
    assertThat(itemService.update("ITEM-001", itemRequest)).isEqualTo(itemResponse);
    itemService.delete("ITEM-001");
    itemService.uploadItemImage("ITEM-001", filePayload);
    assertThat(supplierService.findAll()).containsExactly(supplierResponse);
    assertThat(supplierService.findById("20-12345678-9")).isEqualTo(supplierResponse);
    assertThat(supplierService.update("20-12345678-9", supplierRequest)).isEqualTo(supplierResponse);
    supplierService.delete("20-12345678-9");

    verify(itemQuery).findAll();
    verify(itemQuery).getItemsByCategoryId(1L);
    verify(itemCommand).update("ITEM-001", itemRequest);
    verify(itemCommand).delete("ITEM-001");
    verify(itemCommand).uploadItemImage("ITEM-001", filePayload);
    verify(supplierQuery).findAll();
    verify(supplierQuery).findById("20-12345678-9");
    verify(supplierCommand).update("20-12345678-9", supplierRequest);
    verify(supplierCommand).delete("20-12345678-9");
  }
}
