package disenodesistemas.backendfunerariaapp.modern.application.usecase.income;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import disenodesistemas.backendfunerariaapp.application.support.IncomeDetailService;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.SupplierEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("IncomeCommandUseCase")
class IncomeCommandUseCaseTest {

  @Mock private IncomePersistencePort incomePersistencePort;
  @Mock private IncomeMapper incomeMapper;
  @Mock private ReceiptTypeMapper receiptTypeMapper;
  @Mock private IncomeDetailService incomeDetailService;
  @Mock private UserQueryUseCase userQueryUseCase;
  @Mock private SupplierQueryUseCase supplierQueryUseCase;
  @Mock private ReceiptNumberGeneratorPort receiptNumberGeneratorPort;
  @Mock private IncomeQueryUseCase incomeQueryUseCase;

  @InjectMocks private IncomeCommandUseCase incomeCommandUseCase;

  @Test
  @DisplayName(
      "Given a valid income request when the income is created then it assigns receipt numbers, resolves references, recalculates totals and persists the aggregate")
  void givenAValidIncomeRequestWhenTheIncomeIsCreatedThenItAssignsReceiptNumbersResolvesReferencesRecalculatesTotalsAndPersistsTheAggregate() {
    final IncomeRequestDto request = incomeRequestDto(List.of(incomeDetailRequestDto()));
    final IncomeEntity incomeEntity = new IncomeEntity();
    final UserEntity incomeUser = SecurityTestDataFactory.userEntity();
    final SupplierEntity supplierEntity =
        new SupplierEntity("Proveedor Uno", "20-12345678-9", "https://supplier.example", "proveedor@example.com");
    final ReceiptTypeEntity receiptTypeEntity = new ReceiptTypeEntity("Factura A");
    final IncomeDetailEntity detailEntity = new IncomeDetailEntity();
    final IncomeResponseDto expectedResponse =
        new IncomeResponseDto(
            "7002",
            "1001",
            null,
            null,
            new BigDecimal("21.00"),
            new BigDecimal("242.00"),
            null,
            null,
            null,
            null,
            List.of());

    when(incomeMapper.toEntity(request)).thenReturn(incomeEntity);
    when(receiptNumberGeneratorPort.nextSerialNumber()).thenReturn(1001L);
    when(receiptNumberGeneratorPort.nextReceiptNumber()).thenReturn(7002L);
    when(userQueryUseCase.getUserByEmail(request.incomeUser().email())).thenReturn(incomeUser);
    when(receiptTypeMapper.toEntity(request.receiptType())).thenReturn(receiptTypeEntity);
    when(supplierQueryUseCase.findSupplierEntityByNif(request.supplier().nif()))
        .thenReturn(supplierEntity);
    when(incomeDetailService.mapDetails(request.incomeDetails())).thenReturn(List.of(detailEntity));
    when(incomeDetailService.calculateTotal(List.of(detailEntity), request.tax()))
        .thenReturn(new BigDecimal("242.00"));
    when(incomePersistencePort.save(incomeEntity)).thenReturn(incomeEntity);
    when(incomeMapper.toDto(incomeEntity)).thenReturn(expectedResponse);

    final IncomeResponseDto response = incomeCommandUseCase.create(request);

    assertThat(response).isEqualTo(expectedResponse);
    assertThat(incomeEntity.getReceiptSeries()).isEqualTo(1001L);
    assertThat(incomeEntity.getReceiptNumber()).isEqualTo(7002L);
    assertThat(incomeEntity.getIncomeUser()).isEqualTo(incomeUser);
    assertThat(incomeEntity.getSupplier()).isEqualTo(supplierEntity);
    assertThat(incomeEntity.getReceiptType()).isEqualTo(receiptTypeEntity);
    assertThat(incomeEntity.isDeleted()).isFalse();
    assertThat(incomeEntity.getTotalAmount()).isEqualByComparingTo("242.00");
    verify(incomeDetailService).applyStockAndRefreshPrices(incomeEntity.getIncomeDetails());
  }

  @Test
  @DisplayName(
      "Given an income request without details when the income is created then it initializes an empty detail collection and a zero total amount")
  void givenAnIncomeRequestWithoutDetailsWhenTheIncomeIsCreatedThenItInitializesAnEmptyDetailCollectionAndAZeroTotalAmount() {
    final IncomeRequestDto request = incomeRequestDto(List.of());
    final IncomeEntity incomeEntity = new IncomeEntity();

    when(incomeMapper.toEntity(request)).thenReturn(incomeEntity);
    when(receiptNumberGeneratorPort.nextSerialNumber()).thenReturn(1001L);
    when(receiptNumberGeneratorPort.nextReceiptNumber()).thenReturn(7002L);
    when(userQueryUseCase.getUserByEmail(request.incomeUser().email()))
        .thenReturn(SecurityTestDataFactory.userEntity());
    when(receiptTypeMapper.toEntity(request.receiptType()))
        .thenReturn(new ReceiptTypeEntity("Factura A"));
    when(supplierQueryUseCase.findSupplierEntityByNif(request.supplier().nif()))
        .thenReturn(new SupplierEntity("Proveedor Uno", "20-12345678-9", null, "proveedor@example.com"));
    when(incomePersistencePort.save(incomeEntity)).thenReturn(incomeEntity);
    when(incomeMapper.toDto(incomeEntity))
        .thenReturn(
            new IncomeResponseDto(
                "7002",
                "1001",
                null,
                null,
                request.tax(),
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                List.of()));

    incomeCommandUseCase.create(request);

    assertThat(incomeEntity.getIncomeDetails()).isEmpty();
    assertThat(incomeEntity.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    verify(incomeDetailService, never()).applyStockAndRefreshPrices(List.of());
  }

  @Test
  @DisplayName(
      "Given an income already marked as deleted when the delete use case is executed then it rejects the duplicate delete")
  void givenAnIncomeAlreadyMarkedAsDeletedWhenTheDeleteUseCaseIsExecutedThenItRejectsTheDuplicateDelete() {
    final IncomeEntity deletedIncome = new IncomeEntity();
    deletedIncome.setDeleted(true);

    when(incomeQueryUseCase.findEntityByReceiptNumber(7002L)).thenReturn(deletedIncome);

    assertThatThrownBy(() -> incomeCommandUseCase.delete(7002L))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("income.error.already.deleted");
  }

  @Test
  @DisplayName(
      "Given an active income when the delete use case is executed then it marks the aggregate as deleted and persists the change")
  void givenAnActiveIncomeWhenTheDeleteUseCaseIsExecutedThenItMarksTheAggregateAsDeletedAndPersistsTheChange() {
    final IncomeEntity incomeEntity = new IncomeEntity();
    incomeEntity.setDeleted(false);

    when(incomeQueryUseCase.findEntityByReceiptNumber(7002L)).thenReturn(incomeEntity);

    incomeCommandUseCase.delete(7002L);

    assertThat(incomeEntity.isDeleted()).isTrue();
    verify(incomePersistencePort).save(incomeEntity);
  }

  private IncomeRequestDto incomeRequestDto(final List<IncomeDetailRequestDto> details) {
    return IncomeRequestDto.builder()
        .receiptNumber(7002L)
        .receiptSeries(1001L)
        .tax(new BigDecimal("21.00"))
        .incomeUser(UserDto.builder().email("john.doe@example.com").firstName("John").lastName("Doe").build())
        .receiptType(ReceiptTypeDto.builder().id(1L).name("Factura A").build())
        .supplier(
            SupplierRequestDto.builder()
                .id(1L)
                .name("Proveedor Uno")
                .nif("20-12345678-9")
                .email("proveedor@example.com")
                .webPage("https://supplier.example")
                .build())
        .incomeDetails(details)
        .build();
  }

  private IncomeDetailRequestDto incomeDetailRequestDto() {
    return IncomeDetailRequestDto.builder()
        .quantity(2)
        .purchasePrice(new BigDecimal("100.00"))
        .salePrice(new BigDecimal("121.00"))
        .item(
            ItemRequestDto.builder()
                .id(10L)
                .name("Urna")
                .code("URN-001")
                .price(new BigDecimal("121.00"))
                .build())
        .build();
  }
}
