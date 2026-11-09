package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipDuesUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.PaymentMethod;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipHolderRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipPaymentRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DuesGenerationResultDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipAccountDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipDueDto;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dues and payments against a real PostgreSQL (V19): the billable-affiliate JPQL, idempotent
 * generation (unique affiliate + period), FIFO allocation with credit carried into the next
 * month, the holder row lock query and the check constraints. Rolled back per test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class MembershipBillingPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
  private static final int HOLDER_DNI = 20333444;

  @MockitoBean private AuthenticatedUserPort authenticatedUserPort;

  @Autowired private MembershipDuesUseCase duesUseCase;
  @Autowired private MembershipAccountUseCase accountUseCase;
  @Autowired private MembershipHolderUseCase holderUseCase;
  @Autowired private MembershipTariffQueryUseCase tariff;
  @Autowired private AffiliatePersistencePort affiliatePort;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private EntityManager entityManager;

  private final YearMonth current = YearMonth.now(BUSINESS_ZONE);
  private final YearMonth previous = current.minusMonths(1);
  private MembershipHolderEntity holder;

  @BeforeEach
  void seed() {
    final UserEntity admin = new UserEntity();
    admin.setEmail("admin@funeraria.local");
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(admin);
    jdbcTemplate.update("insert into genders (id, name) values (9201, 'Femenino IT')");
    jdbcTemplate.update("insert into relationships (id, name) values (9201, 'Hijo IT')");
    holder =
        holderUseCase.resolveForEnrolment(
            MembershipHolderRequestDto.builder()
                .dni(HOLDER_DNI)
                .firstName("Rosa")
                .lastName("Gomez")
                .build(),
            null);
    // STANDARD x1.00, 36-50 x1.30 -> 2600.00 each: born on Jan 1st 40 years ago, so 39 or 40 in
    // every month this test bills, whatever year it runs.
    enrol(41000001, true);
    enrol(41000002, true);
    enrol(41000003, false); // legacy: no holder, no tier
    // @CreatedDate stamps today on insert; backdate the enrolment so past months are billable.
    entityManager.flush();
    jdbcTemplate.update(
        "update affiliates set start_date = ? where dni between 41000001 and 41000003",
        current.minusMonths(3).atDay(1));
  }

  private void enrol(final int dni, final boolean billable) {
    final AffiliateEntity.AffiliateEntityBuilder builder =
        AffiliateEntity.builder()
            .dni(dni)
            .firstName("Afiliado")
            .lastName("Gomez")
            .birthDate(LocalDate.of(current.getYear() - 40, 1, 1))
            .startDate(current.minusMonths(3).atDay(1))
            .deceased(false)
            .gender(entityManager.getReference(GenderEntity.class, 9201L))
            .relationship(entityManager.getReference(RelationshipEntity.class, 9201L));
    if (billable) {
      final MembershipPrice price = tariff.priceForEnrolment(40, "STANDARD");
      builder
          .holder(holder)
          .healthTier(price.tier())
          .ageBand(price.band())
          .monthlyFee(price.monthlyFee())
          .feeCalculatedOn(previous.atDay(1));
    }
    affiliatePort.save(builder.build());
  }

  private static MembershipPaymentRequestDto payment(final String amount) {
    return MembershipPaymentRequestDto.builder()
        .amount(new BigDecimal(amount))
        .paidOn(LocalDate.now(BUSINESS_ZONE))
        .method(PaymentMethod.CASH)
        .build();
  }

  @Test
  @DisplayName("Given billable and legacy affiliates when generating a month twice then dues are created once and legacy rows are reported")
  void generationIsIdempotent() {
    final DuesGenerationResultDto first = duesUseCase.generateAsCurrentUser(previous);
    final DuesGenerationResultDto second = duesUseCase.generateAsCurrentUser(previous);

    assertThat(first.created()).isEqualTo(2);
    assertThat(first.unbillable()).isGreaterThanOrEqualTo(1);
    assertThat(second.created()).isZero();
    assertThat(second.alreadyBilled()).isEqualTo(2);
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from membership_dues where holder_id = ?",
                Integer.class,
                holder.getId()))
        .isEqualTo(2);
  }

  @Test
  @DisplayName("Given an overpayment when the next month is generated then the credit is applied to the new dues")
  void paymentsSettleOldestFirstAndCarryCredit() {
    duesUseCase.generateAsCurrentUser(previous);
    entityManager.flush();

    final MembershipAccountDto afterPayment =
        accountUseCase.registerPayment(HOLDER_DNI, payment("6000.00"));

    assertThat(afterPayment.outstanding()).isEqualByComparingTo("0.00");
    assertThat(afterPayment.credit()).isEqualByComparingTo("800.00");
    assertThat(afterPayment.balance()).isEqualByComparingTo("-800.00");
    assertThat(afterPayment.dues()).extracting(MembershipDueDto::status).containsOnly("PAID");

    duesUseCase.generateCurrentMonth();
    entityManager.flush();
    entityManager.clear();

    final MembershipAccountDto account = accountUseCase.statement(HOLDER_DNI);
    assertThat(account.totalBilled()).isEqualByComparingTo("10400.00");
    assertThat(account.totalPaid()).isEqualByComparingTo("6000.00");
    assertThat(account.credit()).isEqualByComparingTo("0.00");
    assertThat(account.outstanding()).isEqualByComparingTo("4400.00");
    assertThat(account.dues()).hasSize(4);
    assertThat(account.dues().get(2).paidAmount()).isEqualByComparingTo("800.00");
    assertThat(account.payments()).singleElement().satisfies(
        p -> assertThat(p.registeredBy()).isEqualTo("admin@funeraria.local"));
  }

  @Test
  @DisplayName("Given an unpaid due past its due date when reading the statement then it counts as overdue")
  void overdueCount() {
    duesUseCase.generateAsCurrentUser(previous.minusMonths(1));
    entityManager.flush();

    final MembershipAccountDto account = accountUseCase.statement(HOLDER_DNI);

    assertThat(account.overdueCount()).isEqualTo(2);
    assertThat(account.balance()).isEqualByComparingTo("5200.00");
  }

  @Test
  @DisplayName("Given a billed month when inserting the same affiliate and period again then the unique key rejects it")
  void constraintsHold() {
    duesUseCase.generateAsCurrentUser(previous);
    entityManager.flush();
    final Long dueId =
        jdbcTemplate.queryForObject(
            "select min(id) from membership_dues where holder_id = ?", Long.class, holder.getId());

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "insert into membership_dues (affiliate_id, holder_id, period, due_date, amount, health_tier_id)"
                        + " select affiliate_id, holder_id, period, due_date, amount, health_tier_id"
                        + " from membership_dues where id = ?",
                    dueId))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("Given a due when paying more than its amount at the SQL level then the check constraint rejects it")
  void overpaidDueRejected() {
    duesUseCase.generateAsCurrentUser(previous);
    entityManager.flush();

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    "update membership_dues set paid_amount = amount + 1 where holder_id = ?",
                    holder.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
