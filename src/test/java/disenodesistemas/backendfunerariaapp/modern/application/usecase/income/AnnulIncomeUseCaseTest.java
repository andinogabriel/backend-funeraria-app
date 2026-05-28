package disenodesistemas.backendfunerariaapp.modern.application.usecase.income;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import disenodesistemas.backendfunerariaapp.application.support.IncomeDetailService;
import disenodesistemas.backendfunerariaapp.application.usecase.income.AnnulIncomeUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import disenodesistemas.backendfunerariaapp.domain.event.IncomeAnnulled;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.IncomeMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("AnnulIncomeUseCase")
class AnnulIncomeUseCaseTest {

  @Mock private IncomePersistencePort incomePersistencePort;
  @Mock private IncomeQueryUseCase incomeQueryUseCase;
  @Mock private IncomeDetailService incomeDetailService;
  @Mock private IncomeMapper incomeMapper;
  @Mock private ReceiptNumberGeneratorPort receiptNumberGeneratorPort;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AuditEventPort auditEventPort;
  @Mock private OutboxPort outboxPort;

  @InjectMocks private AnnulIncomeUseCase annulIncomeUseCase;

  @Test
  @DisplayName(
      "Given an ACTIVE income with sufficient stock when annul runs then it restores stock, persists a reversal with negative quantities, marks the original as ANNULLED, and emits audit + outbox")
  void annulHappyPath() {
    final ItemEntity item = item("ATAUD-01", 20);
    final UserEntity actor = SecurityTestDataFactory.userEntity();
    final IncomeEntity original = activeIncome(10L);
    final IncomeDetailEntity detail = detail(item, 5, "100.00");
    original.setIncomeDetails(List.of(detail));

    when(incomeQueryUseCase.findEntityById(10L)).thenReturn(original);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);
    when(receiptNumberGeneratorPort.nextReceiptNumber()).thenReturn(7777L);
    when(receiptNumberGeneratorPort.nextSerialNumber()).thenReturn(8888L);
    when(incomeDetailService.calculateTotal(anyList(), any(BigDecimal.class)))
        .thenReturn(new BigDecimal("-605.00"));
    final IncomeEntity savedReversal = activeIncome(99L);
    when(incomePersistencePort.save(any(IncomeEntity.class)))
        .thenAnswer(invocation -> {
          final IncomeEntity arg = invocation.getArgument(0);
          // Stamp the saved reversal id only on the FIRST save (the reversal one);
          // the second save is the original being marked ANNULLED.
          return arg == original ? original : savedReversal;
        });
    final IncomeResponseDto mappedReversal =
        new IncomeResponseDto(
            99L,
            "7777",
            "8888",
            null,
            null,
            new BigDecimal("21.00"),
            new BigDecimal("-605.00"),
            null,
            null,
            null,
            null,
            List.of(),
            IncomeStatus.ACTIVE,
            10L);
    when(incomeMapper.toDto(savedReversal)).thenReturn(mappedReversal);

    final IncomeResponseDto response = annulIncomeUseCase.annul(10L);

    assertThat(response).isEqualTo(mappedReversal);
    assertThat(original.getStatus()).isEqualTo(IncomeStatus.ANNULLED);

    // Stock decremented via the existing restoreStock helper — same direction the income
    // update flow uses to roll back a previous increment.
    verify(incomeDetailService).restoreStock(original.getIncomeDetails());

    // Reversal payload: negative qty, same prices, fresh receipt numbers, link to original.
    final ArgumentCaptor<IncomeEntity> captor = ArgumentCaptor.forClass(IncomeEntity.class);
    verify(incomePersistencePort, org.mockito.Mockito.times(2)).save(captor.capture());
    final IncomeEntity capturedReversal = captor.getAllValues().get(0);
    assertThat(capturedReversal.getReceiptNumber()).isEqualTo(7777L);
    assertThat(capturedReversal.getReceiptSeries()).isEqualTo(8888L);
    assertThat(capturedReversal.getReversalOf()).isSameAs(original);
    assertThat(capturedReversal.getStatus()).isEqualTo(IncomeStatus.ACTIVE);
    assertThat(capturedReversal.getIncomeDetails()).hasSize(1);
    final IncomeDetailEntity revDetail = capturedReversal.getIncomeDetails().get(0);
    assertThat(revDetail.getQuantity()).isEqualTo(-5);
    assertThat(revDetail.getPurchasePrice()).isEqualByComparingTo("100.00");
    assertThat(revDetail.getItem()).isSameAs(item);

    // Audit + outbox carry both ids so consumers can join the pair.
    verify(auditEventPort)
        .record(
            eq(AuditAction.INCOME_ANNULLED),
            eq(actor.getEmail()),
            eq(actor.getId()),
            eq("INCOME"),
            eq("10"),
            eq("{\"originalId\":10,\"reversalId\":99}"));
    verify(outboxPort).publish(new IncomeAnnulled(10L, 99L));
  }

  @Test
  @DisplayName(
      "Given an already ANNULLED income when annul runs then it rejects the command with 409 and never writes anything")
  void annulIsStrictlyIdempotentSecondCallReturns409() {
    final IncomeEntity annulled = activeIncome(10L);
    annulled.setStatus(IncomeStatus.ANNULLED);
    when(incomeQueryUseCase.findEntityById(10L)).thenReturn(annulled);

    assertThatThrownBy(() -> annulIncomeUseCase.annul(10L))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("income.error.annul.already.annulled");

    // Strict idempotency: every side-effect is skipped, including stock and audit.
    verify(incomeDetailService, never()).restoreStock(anyList());
    verify(incomePersistencePort, never()).save(any());
    verify(auditEventPort, never()).record(any(), any(), any(), any(), any(), any());
    verify(outboxPort, never()).publish(any());
  }

  @Test
  @DisplayName(
      "Given a reversal counter-entry when annul runs then it rejects the command with 409 because reversal rows are immutable by accounting contract")
  void annulCannotTargetAReversalCounterEntry() {
    final IncomeEntity original = activeIncome(10L);
    final IncomeEntity reversal = activeIncome(11L);
    reversal.setReversalOf(original);
    when(incomeQueryUseCase.findEntityById(11L)).thenReturn(reversal);

    assertThatThrownBy(() -> annulIncomeUseCase.annul(11L))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("income.error.annul.is.reversal");

    verify(incomePersistencePort, never()).save(any());
  }

  @Test
  @DisplayName(
      "Given an income where at least one item has stock below the original quantity when annul runs then it rejects the command with 409 and writes nothing")
  void annulRefusesWhenAnyItemWouldGoNegative() {
    final ItemEntity ataud = item("ATAUD-01", 1); // less than the 5 we received
    final IncomeEntity original = activeIncome(10L);
    original.setIncomeDetails(List.of(detail(ataud, 5, "100.00")));
    when(incomeQueryUseCase.findEntityById(10L)).thenReturn(original);

    assertThatThrownBy(() -> annulIncomeUseCase.annul(10L))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("income.error.annul.insufficient.stock");

    // Stock check fires BEFORE any persistence write so the operator sees a clean 409
    // instead of a half-applied annul.
    verify(incomeDetailService, never()).restoreStock(anyList());
    verify(incomePersistencePort, never()).save(any());
    verify(auditEventPort, never()).record(any(), any(), any(), any(), any(), any());
  }

  /* ------------------------------- helpers -------------------------------- */

  private static IncomeEntity activeIncome(final long id) {
    final IncomeEntity entity = new IncomeEntity();
    entity.setId(id);
    entity.setStatus(IncomeStatus.ACTIVE);
    entity.setTax(new BigDecimal("21.00"));
    return entity;
  }

  private static IncomeDetailEntity detail(
      final ItemEntity item, final int qty, final String price) {
    return IncomeDetailEntity.builder()
        .item(item)
        .quantity(qty)
        .purchasePrice(new BigDecimal(price))
        .salePrice(new BigDecimal(price))
        .build();
  }

  private static ItemEntity item(final String code, final int stock) {
    final ItemEntity entity = new ItemEntity();
    entity.setCode(code);
    entity.setStock(stock);
    return entity;
  }
}
