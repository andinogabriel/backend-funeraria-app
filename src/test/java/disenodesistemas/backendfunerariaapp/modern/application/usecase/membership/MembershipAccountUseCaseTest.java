package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipHolderPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipPaymentEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.enums.PaymentMethod;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipPaymentRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipAccountDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipDueDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderDetailDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderResponseDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Statement figures and FIFO allocation of payments (ADR-0021). */
@ExtendWith(MockitoExtension.class)
class MembershipAccountUseCaseTest {

  /** 2026-11-15 12:00 in Buenos Aires: November dues (due on the 10th) are overdue. */
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-15T15:00:00Z"), ZoneOffset.UTC);

  private static final int DNI = 20111222;
  private static final long HOLDER_ID = 7L;

  @Mock private MembershipBillingPersistencePort billingPort;
  @Mock private MembershipHolderPersistencePort holderPort;
  @Mock private MembershipHolderUseCase holderUseCase;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AuditEventPort auditEventPort;

  private MembershipAccountUseCase useCase;
  private final AffiliateEntity ana =
      AffiliateEntity.builder().id(1L).dni(40111222).firstName("Ana").lastName("Gomez").build();
  private final MembershipHolderEntity holder =
      MembershipHolderEntity.builder().id(HOLDER_ID).dni(DNI).build();

  @BeforeEach
  void setUp() {
    useCase =
        new MembershipAccountUseCase(
            billingPort, holderPort, holderUseCase, authenticatedUserPort, auditEventPort, CLOCK);
  }

  private MembershipDueEntity due(final String period, final String amount, final String paid) {
    final LocalDate first = LocalDate.parse(period + "-01");
    return MembershipDueEntity.builder()
        .id((long) first.getMonthValue())
        .affiliate(ana)
        .holder(holder)
        .period(first)
        .dueDate(first.withDayOfMonth(10))
        .amount(new BigDecimal(amount))
        .paidAmount(new BigDecimal(paid))
        .build();
  }

  private static MembershipPaymentEntity payment(final String amount, final String unallocated) {
    return MembershipPaymentEntity.builder()
        .id(1L)
        .amount(new BigDecimal(amount))
        .unallocated(new BigDecimal(unallocated))
        .paidOn(LocalDate.of(2026, 11, 1))
        .method(PaymentMethod.CASH)
        .registeredBy("admin@funeraria.local")
        .build();
  }

  private void stubHolderDetail() {
    when(holderUseCase.getByDni(DNI))
        .thenReturn(
            new MembershipHolderDetailDto(
                new MembershipHolderResponseDto(HOLDER_ID, DNI, "Rosa", "Gomez", null, null),
                1,
                new BigDecimal("2600.00"),
                List.of()));
  }

  @Test
  @DisplayName("Given credit spread over two payments when settling then the oldest dues are paid first and the rest stays as credit")
  void settleAllocatesOldestFirst() {
    final MembershipDueEntity october = due("2026-10", "2600.00", "1000.00");
    final MembershipDueEntity november = due("2026-11", "2600.00", "0.00");
    final MembershipPaymentEntity first = payment("1000.00", "1000.00");
    final MembershipPaymentEntity second = payment("4000.00", "4000.00");
    when(billingPort.findPaymentsWithCreditOf(HOLDER_ID)).thenReturn(List.of(first, second));
    when(billingPort.findOpenDuesOf(HOLDER_ID)).thenReturn(List.of(october, november));

    useCase.settle(HOLDER_ID);

    assertThat(october.getPaidAmount()).isEqualByComparingTo("2600.00");
    assertThat(november.getPaidAmount()).isEqualByComparingTo("2600.00");
    assertThat(first.getUnallocated()).isEqualByComparingTo("0.00");
    assertThat(second.getUnallocated()).isEqualByComparingTo("800.00");
  }

  @Test
  @DisplayName("Given less credit than debt when settling then the oldest due is partially paid and newer ones untouched")
  void settlePartial() {
    final MembershipDueEntity october = due("2026-10", "2600.00", "0.00");
    final MembershipDueEntity november = due("2026-11", "2600.00", "0.00");
    final MembershipPaymentEntity credit = payment("1000.00", "1000.00");
    when(billingPort.findPaymentsWithCreditOf(HOLDER_ID)).thenReturn(List.of(credit));
    when(billingPort.findOpenDuesOf(HOLDER_ID)).thenReturn(List.of(october, november));

    useCase.settle(HOLDER_ID);

    assertThat(october.getPaidAmount()).isEqualByComparingTo("1000.00");
    assertThat(november.getPaidAmount()).isEqualByComparingTo("0.00");
    assertThat(credit.getUnallocated()).isEqualByComparingTo("0.00");
  }

  @Test
  @DisplayName("Given no credit when settling then open dues are not even read")
  void settleWithoutCredit() {
    when(billingPort.findPaymentsWithCreditOf(HOLDER_ID)).thenReturn(List.of());

    useCase.settle(HOLDER_ID);

    verify(billingPort, never()).findOpenDuesOf(any());
  }

  @Test
  @DisplayName("Given paid, partial, pending and overdue dues when reading the statement then totals, balance and statuses match")
  void statementFigures() {
    stubHolderDetail();
    final MembershipDueEntity september = due("2026-09", "2600.00", "2600.00");
    final MembershipDueEntity october = due("2026-10", "2600.00", "600.00");
    final MembershipDueEntity december = due("2026-12", "2600.00", "500.00");
    final MembershipDueEntity january = due("2027-01", "2600.00", "0.00");
    when(billingPort.findDuesOf(HOLDER_ID)).thenReturn(List.of(september, october, december, january));
    when(billingPort.findPaymentsOf(HOLDER_ID))
        .thenReturn(List.of(payment("3700.00", "0.00"), payment("500.00", "200.00")));

    final MembershipAccountDto account = useCase.statement(DNI);

    assertThat(account.totalBilled()).isEqualByComparingTo("10400.00");
    assertThat(account.totalPaid()).isEqualByComparingTo("4200.00");
    assertThat(account.outstanding()).isEqualByComparingTo("6700.00");
    assertThat(account.credit()).isEqualByComparingTo("200.00");
    assertThat(account.balance()).isEqualByComparingTo("6500.00");
    assertThat(account.overdueCount()).isEqualTo(1);
    assertThat(account.monthlyTotal()).isEqualByComparingTo("2600.00");
    assertThat(account.dues())
        .extracting(MembershipDueDto::status)
        .containsExactly("PAID", "OVERDUE", "PARTIAL", "PENDING");
    assertThat(account.dues().getFirst().affiliateName()).isEqualTo("Ana Gomez");
    assertThat(account.dues().getFirst().period()).hasToString("2026-09");
  }

  @Test
  @DisplayName("Given a payment when registering it then the holder is locked before saving, credit is settled and the event audited")
  void registerPayment() {
    final UserEntity admin = new UserEntity();
    admin.setId(3L);
    admin.setEmail("admin@funeraria.local");
    when(holderPort.findByDni(DNI)).thenReturn(Optional.of(holder));
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(admin);
    when(billingPort.findPaymentsWithCreditOf(HOLDER_ID)).thenReturn(List.of());
    stubHolderDetail();

    useCase.registerPayment(
        DNI,
        MembershipPaymentRequestDto.builder()
            .amount(new BigDecimal("2600"))
            .paidOn(LocalDate.of(2026, 11, 14))
            .method(PaymentMethod.TRANSFER)
            .reference("  ")
            .build());

    final InOrder order = inOrder(billingPort);
    order.verify(billingPort).lockHolder(HOLDER_ID);
    final ArgumentCaptor<MembershipPaymentEntity> saved =
        ArgumentCaptor.forClass(MembershipPaymentEntity.class);
    order.verify(billingPort).savePayment(saved.capture());
    order.verify(billingPort).findPaymentsWithCreditOf(HOLDER_ID);
    assertThat(saved.getValue().getAmount()).isEqualByComparingTo("2600.00");
    assertThat(saved.getValue().getUnallocated()).isEqualByComparingTo("2600.00");
    assertThat(saved.getValue().getReference()).isNull();
    assertThat(saved.getValue().getRegisteredBy()).isEqualTo("admin@funeraria.local");
    verify(auditEventPort)
        .record(
            eq(AuditAction.MEMBERSHIP_PAYMENT_REGISTERED),
            eq("admin@funeraria.local"),
            eq(3L),
            eq("MEMBERSHIP_HOLDER"),
            eq(String.valueOf(DNI)),
            contains("\"method\":\"TRANSFER\""));
  }

  @Test
  @DisplayName("Given an unknown holder DNI when registering a payment then it is a 404 and nothing is saved")
  void registerPaymentUnknownHolder() {
    when(holderPort.findByDni(DNI)).thenReturn(Optional.empty());
    final MembershipPaymentRequestDto request =
        MembershipPaymentRequestDto.builder()
            .amount(BigDecimal.TEN)
            .paidOn(LocalDate.of(2026, 11, 14))
            .method(PaymentMethod.CASH)
            .build();

    assertThatThrownBy(() -> useCase.registerPayment(DNI, request))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("holder.error.not.found");
    verify(billingPort, never()).savePayment(any());
  }
}
