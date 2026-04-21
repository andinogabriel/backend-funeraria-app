package disenodesistemas.backendfunerariaapp.modern.application.usecase.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.model.FilePayload;
import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.SupplierPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.category.CategoryQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.item.ItemQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AddressMapper;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.mapping.ItemMapper;
import disenodesistemas.backendfunerariaapp.mapping.MobileNumberMapper;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.mapping.SupplierMapper;
import disenodesistemas.backendfunerariaapp.modern.support.DomainTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.web.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.CityDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.CategoryResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ItemResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("Inventory Use Cases")
class InventoryUseCasesTest {

  @Test
  @DisplayName(
      "Given an item request when the item is created then it assigns a generated code and persists the aggregate")
  void givenAnItemRequestWhenTheItemIsCreatedThenItAssignsAGeneratedCodeAndPersistsTheAggregate() {
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final ItemMapper itemMapper = mock(ItemMapper.class);
    final FileStoragePort fileStoragePort = mock(FileStoragePort.class);
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, itemMapper, mock(CategoryQueryUseCase.class));
    final ItemCommandUseCase itemCommandUseCase =
        new ItemCommandUseCase(itemPersistencePort, itemMapper, fileStoragePort, itemQueryUseCase);
    final ItemRequestDto request =
        ItemRequestDto.builder()
            .name(TestValues.ITEM_NAME)
            .description(TestValues.ITEM_DESCRIPTION)
            .price(new BigDecimal("100.00"))
            .build();
    final ItemEntity itemEntity = new ItemEntity();
    final ItemResponseDto response =
        new ItemResponseDto(
            TestValues.ITEM_NAME,
            TestValues.ITEM_DESCRIPTION,
            "generated-code",
            null,
            null,
            new BigDecimal("100.00"),
            null,
            null,
            null,
            null,
            null);

    when(itemMapper.toEntity(request)).thenReturn(itemEntity);
    when(itemPersistencePort.save(itemEntity)).thenReturn(itemEntity);
    when(itemMapper.toDto(itemEntity)).thenReturn(response);

    final ItemResponseDto created = itemCommandUseCase.create(request);

    assertThat(created).isEqualTo(response);
    assertThat(itemEntity.getCode()).isNotBlank();
    verify(itemPersistencePort).save(itemEntity);
  }

  @Test
  @DisplayName(
      "Given an item with a stored image when the item is deleted then it removes the external file and deletes the aggregate")
  void givenAnItemWithAStoredImageWhenTheItemIsDeletedThenItRemovesTheExternalFileAndDeletesTheAggregate() {
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final FileStoragePort fileStoragePort = mock(FileStoragePort.class);
    final ItemEntity itemEntity = DomainTestDataFactory.itemEntity();
    itemEntity.setItemImageLink("https://bucket/items/ITEM-001/image.png");
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, mock(ItemMapper.class), mock(CategoryQueryUseCase.class));
    final ItemCommandUseCase itemCommandUseCase =
        new ItemCommandUseCase(itemPersistencePort, mock(ItemMapper.class), fileStoragePort, itemQueryUseCase);

    when(itemPersistencePort.findByCode(TestValues.ITEM_CODE)).thenReturn(Optional.of(itemEntity));

    itemCommandUseCase.delete(TestValues.ITEM_CODE);

    verify(fileStoragePort).deleteFiles(itemEntity);
    verify(itemPersistencePort).delete(itemEntity);
  }

  @Test
  @DisplayName(
      "Given an existing item when it is updated then the use case mutates the aggregate through the mapper and persists the changes")
  void givenAnExistingItemWhenItIsUpdatedThenTheUseCaseMutatesTheAggregateThroughTheMapperAndPersistsTheChanges() {
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final ItemMapper itemMapper = mock(ItemMapper.class);
    final FileStoragePort fileStoragePort = mock(FileStoragePort.class);
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, itemMapper, mock(CategoryQueryUseCase.class));
    final ItemCommandUseCase itemCommandUseCase =
        new ItemCommandUseCase(itemPersistencePort, itemMapper, fileStoragePort, itemQueryUseCase);
    final ItemRequestDto request =
        ItemRequestDto.builder().name("Urna actualizada").description("Nueva descripcion").build();
    final ItemEntity itemEntity = DomainTestDataFactory.itemEntity();
    final ItemResponseDto response =
        new ItemResponseDto(
            "Urna actualizada",
            "Nueva descripcion",
            TestValues.ITEM_CODE,
            null,
            null,
            new BigDecimal("150.00"),
            null,
            null,
            null,
            null,
            null);

    when(itemPersistencePort.findByCode(TestValues.ITEM_CODE)).thenReturn(Optional.of(itemEntity));
    when(itemPersistencePort.save(itemEntity)).thenReturn(itemEntity);
    when(itemMapper.toDto(itemEntity)).thenReturn(response);

    final ItemResponseDto updated = itemCommandUseCase.update(TestValues.ITEM_CODE, request);

    assertThat(updated).isEqualTo(response);
    verify(itemMapper).updateEntity(request, itemEntity);
    verify(itemPersistencePort).save(itemEntity);
  }

  @Test
  @DisplayName(
      "Given a null image payload when an item image upload is requested then it skips storage and persistence")
  void givenANullImagePayloadWhenAnItemImageUploadIsRequestedThenItSkipsStorageAndPersistence() {
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final FileStoragePort fileStoragePort = mock(FileStoragePort.class);
    final ItemEntity itemEntity = DomainTestDataFactory.itemEntity();
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, mock(ItemMapper.class), mock(CategoryQueryUseCase.class));
    final ItemCommandUseCase itemCommandUseCase =
        new ItemCommandUseCase(itemPersistencePort, mock(ItemMapper.class), fileStoragePort, itemQueryUseCase);

    when(itemPersistencePort.findByCode(TestValues.ITEM_CODE)).thenReturn(Optional.of(itemEntity));

    itemCommandUseCase.uploadItemImage(TestValues.ITEM_CODE, null);

    verify(fileStoragePort, never()).store(any(), any());
    verify(itemPersistencePort, never()).save(any());
  }

  @Test
  @DisplayName(
      "Given a valid image payload when an item image upload is requested then it stores the file and persists the generated link")
  void givenAValidImagePayloadWhenAnItemImageUploadIsRequestedThenItStoresTheFileAndPersistsTheGeneratedLink() {
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final FileStoragePort fileStoragePort = mock(FileStoragePort.class);
    final ItemEntity itemEntity = DomainTestDataFactory.itemEntity();
    final FilePayload filePayload = new FilePayload("image.png", "image/png", new byte[] {1, 2, 3});
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, mock(ItemMapper.class), mock(CategoryQueryUseCase.class));
    final ItemCommandUseCase itemCommandUseCase =
        new ItemCommandUseCase(itemPersistencePort, mock(ItemMapper.class), fileStoragePort, itemQueryUseCase);

    when(itemPersistencePort.findByCode(TestValues.ITEM_CODE)).thenReturn(Optional.of(itemEntity));
    when(fileStoragePort.store(itemEntity, filePayload))
        .thenReturn("https://bucket/items/ITEM-001/image.png");

    itemCommandUseCase.uploadItemImage(TestValues.ITEM_CODE, filePayload);

    assertThat(itemEntity.getItemImageLink()).isEqualTo("https://bucket/items/ITEM-001/image.png");
    verify(itemPersistencePort).save(itemEntity);
  }

  @Test
  @DisplayName(
      "Given items grouped by category when they are requested then it resolves the category first and maps the item list")
  void givenItemsGroupedByCategoryWhenTheyAreRequestedThenItResolvesTheCategoryFirstAndMapsTheItemList() {
    final ItemPersistencePort itemPersistencePort = mock(ItemPersistencePort.class);
    final ItemMapper itemMapper = mock(ItemMapper.class);
    final CategoryQueryUseCase categoryQueryUseCase = mock(CategoryQueryUseCase.class);
    final ItemQueryUseCase itemQueryUseCase =
        new ItemQueryUseCase(itemPersistencePort, itemMapper, categoryQueryUseCase);
    final disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity categoryEntity =
        new disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity("Urnas", "Productos funerarios");
    final ItemEntity itemEntity = new ItemEntity();
    final ItemResponseDto response =
        new ItemResponseDto(
            "Urna", "Urna premium", "ITEM-001", null, 5, new BigDecimal("100.00"), null, null, null,
            new CategoryResponseDto(1L, "Urnas", "Productos funerarios"),
            new BrandResponseDto(1L, "Acme", "https://acme.example"));

    when(categoryQueryUseCase.findCategoryEntityById(1L)).thenReturn(categoryEntity);
    when(itemPersistencePort.findByCategoryOrderByName(categoryEntity)).thenReturn(List.of(itemEntity));
    when(itemMapper.toDto(itemEntity)).thenReturn(response);

    assertThat(itemQueryUseCase.getItemsByCategoryId(1L)).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given a supplier update with stale contacts when the supplier is updated then it synchronizes the requested mobiles and addresses before persisting")
  void givenASupplierUpdateWithStaleContactsWhenTheSupplierIsUpdatedThenItSynchronizesTheRequestedMobilesAndAddressesBeforePersisting() {
    final SupplierPersistencePort supplierPersistencePort = mock(SupplierPersistencePort.class);
    final SupplierMapper supplierMapper = mock(SupplierMapper.class);
    final MobileNumberMapper mobileNumberMapper = mock(MobileNumberMapper.class);
    final AddressMapper addressMapper = mock(AddressMapper.class);
    final SupplierQueryUseCase supplierQueryUseCase =
        new SupplierQueryUseCase(supplierPersistencePort, supplierMapper);
    final SupplierCommandUseCase supplierCommandUseCase =
        new SupplierCommandUseCase(
            supplierPersistencePort,
            supplierMapper,
            mobileNumberMapper,
            addressMapper,
            supplierQueryUseCase);
    final SupplierEntity supplierEntity = DomainTestDataFactory.supplierEntity();
    final MobileNumberEntity existingMobile = new MobileNumberEntity("111-111");
    existingMobile.setId(1L);
    final MobileNumberEntity removedMobile = new MobileNumberEntity("222-222");
    removedMobile.setId(2L);
    supplierEntity.setMobileNumbers(List.of(existingMobile, removedMobile));
    final AddressEntity existingAddress = AddressEntity.builder().id(10L).streetName("Siempre Viva").city(CityEntity.builder().id(1L).name("Cordoba").zipCode("5000").build()).build();
    final AddressEntity removedAddress = AddressEntity.builder().id(11L).streetName("Otra").city(CityEntity.builder().id(1L).name("Cordoba").zipCode("5000").build()).build();
    supplierEntity.setAddresses(List.of(existingAddress, removedAddress));

    final MobileNumberRequestDto mobileRequest = MobileNumberRequestDto.builder().id(1L).mobileNumber("111-111").build();
    final MobileNumberRequestDto newMobileRequest = MobileNumberRequestDto.builder().mobileNumber("333-333").build();
    final AddressRequestDto addressRequest =
        AddressRequestDto.builder()
            .id(10L)
            .streetName("Siempre Viva")
            .city(CityDto.builder().id(1L).name("Cordoba").zipCode("5000").build())
            .build();
    final AddressRequestDto newAddressRequest =
        AddressRequestDto.builder()
            .streetName("Nueva")
            .city(CityDto.builder().id(1L).name("Cordoba").zipCode("5000").build())
            .build();
    final SupplierRequestDto request =
        SupplierRequestDto.builder()
            .name("Proveedor Uno")
            .nif(TestValues.SUPPLIER_NIF)
            .email(TestValues.SUPPLIER_EMAIL)
            .webPage(TestValues.SUPPLIER_WEB_PAGE)
            .mobileNumbers(List.of(mobileRequest, newMobileRequest))
            .addresses(List.of(addressRequest, newAddressRequest))
            .build();
    final MobileNumberEntity persistedMobile = new MobileNumberEntity("111-111");
    persistedMobile.setId(1L);
    final MobileNumberEntity newMobile = new MobileNumberEntity("333-333");
    final AddressEntity persistedAddress =
        AddressEntity.builder().id(10L).streetName("Siempre Viva").city(CityEntity.builder().id(1L).name("Cordoba").zipCode("5000").build()).build();
    final AddressEntity newAddress =
        AddressEntity.builder().streetName("Nueva").city(CityEntity.builder().id(1L).name("Cordoba").zipCode("5000").build()).build();
    final SupplierResponseDto response =
        new SupplierResponseDto(
            TestValues.SUPPLIER_NAME,
            TestValues.SUPPLIER_NIF,
            TestValues.SUPPLIER_WEB_PAGE,
            TestValues.SUPPLIER_EMAIL,
            List.of(),
            List.of());

    when(supplierPersistencePort.findByNif(TestValues.SUPPLIER_NIF))
        .thenReturn(Optional.of(supplierEntity));
    when(mobileNumberMapper.toEntity(mobileRequest)).thenReturn(persistedMobile);
    when(mobileNumberMapper.toEntity(newMobileRequest)).thenReturn(newMobile);
    when(addressMapper.toEntity(addressRequest)).thenReturn(persistedAddress);
    when(addressMapper.toEntity(newAddressRequest)).thenReturn(newAddress);
    when(supplierPersistencePort.save(supplierEntity)).thenReturn(supplierEntity);
    when(supplierMapper.toDto(supplierEntity)).thenReturn(response);

    final SupplierResponseDto updated = supplierCommandUseCase.update(TestValues.SUPPLIER_NIF, request);

    assertThat(updated).isEqualTo(response);
    assertThat(supplierEntity.getMobileNumbers()).containsExactly(persistedMobile, newMobile);
    assertThat(supplierEntity.getAddresses()).containsExactly(persistedAddress, newAddress);
    assertThat(newMobile.getSupplierNumber()).isEqualTo(supplierEntity);
    assertThat(newAddress.getSupplierAddress()).isEqualTo(supplierEntity);
  }

  @Test
  @DisplayName(
      "Given a missing supplier nif when the supplier is requested then it throws the application not found exception")
  void givenAMissingSupplierNifWhenTheSupplierIsRequestedThenItThrowsTheApplicationNotFoundException() {
    final SupplierPersistencePort supplierPersistencePort = mock(SupplierPersistencePort.class);
    final SupplierQueryUseCase supplierQueryUseCase =
        new SupplierQueryUseCase(supplierPersistencePort, mock(SupplierMapper.class));

    when(supplierPersistencePort.findByNif("20-99999999-9")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> supplierQueryUseCase.findSupplierEntityByNif("20-99999999-9"))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("supplier.error.not.found");
  }

  @Test
  @DisplayName(
      "Given a paginated income query when incomes are requested then it converts the page to zero based and maps the page content")
  void givenAPaginatedIncomeQueryWhenIncomesAreRequestedThenItConvertsThePageToZeroBasedAndMapsThePageContent() {
    final IncomePersistencePort incomePersistencePort = mock(IncomePersistencePort.class);
    final IncomeMapper incomeMapper = mock(IncomeMapper.class);
    final IncomeQueryUseCase incomeQueryUseCase = new IncomeQueryUseCase(incomePersistencePort, incomeMapper);
    final IncomeEntity incomeEntity = new IncomeEntity();
    final IncomeResponseDto response =
        new IncomeResponseDto("7002", "1001", null, null, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null, null, List.of());
    final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    when(incomePersistencePort.findAllByDeleted(any(Boolean.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(incomeEntity)));
    when(incomeMapper.toDto(incomeEntity)).thenReturn(response);

    assertThat(incomeQueryUseCase.getIncomesPaginated(false, 1, 10, "receiptNumber", "desc").getContent())
        .containsExactly(response);
    verify(incomePersistencePort).findAllByDeleted(any(Boolean.class), pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
  }

  @Test
  @DisplayName(
      "Given a missing plan id when the plan entity is requested then it throws a not found exception")
  void givenAMissingPlanIdWhenThePlanEntityIsRequestedThenItThrowsANotFoundException() {
    final PlanPersistencePort planPersistencePort = mock(PlanPersistencePort.class);
    final PlanQueryUseCase planQueryUseCase = new PlanQueryUseCase(planPersistencePort, mock(PlanMapper.class));

    when(planPersistencePort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> planQueryUseCase.findEntityById(99L))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("plan.error.not.found");
  }

  @Test
  @DisplayName(
      "Given persisted plans when all plans are requested then it maps the ordered plan list")
  void givenPersistedPlansWhenAllPlansAreRequestedThenItMapsTheOrderedPlanList() {
    final PlanPersistencePort planPersistencePort = mock(PlanPersistencePort.class);
    final PlanMapper planMapper = mock(PlanMapper.class);
    final PlanQueryUseCase planQueryUseCase = new PlanQueryUseCase(planPersistencePort, planMapper);
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    final PlanResponseDto response =
        new PlanResponseDto(1L, "Plan Oro", "Cobertura completa", null, null, new BigDecimal("25.00"), java.util.Set.of());

    when(planPersistencePort.findAllByOrderByIdDesc()).thenReturn(List.of(plan));
    when(planMapper.toDto(plan)).thenReturn(response);

    assertThat(planQueryUseCase.findAll()).containsExactly(response);
  }
}
