package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipEligibilityUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipHolderRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ClaimEligibilityDto;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Claim eligibility against a real PostgreSQL (V20, V21): the seeded accidental death cause, the
 * seeded tier waiting periods and grace count, the waiting period counted from the persisted
 * {@code enrolment_date} (not the {@code start_date} creation stamp), and overdue dues read from
 * {@code membership_dues}.
 * Rolled back per test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ClaimEligibilityPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final int AFFILIATE_DNI = 42000001;
  /** Seeded in V2 and flagged accidental in V20. */
  private static final long TRAFFIC_ACCIDENT = 4L;
  /** Seeded in V2, not accidental. */
  private static final long SUDDEN_DEATH = 1L;

  @Autowired private MembershipEligibilityUseCase eligibilityUseCase;
  @Autowired private MembershipHolderUseCase holderUseCase;
  @Autowired private MembershipTariffQueryUseCase tariff;
  @Autowired private AffiliatePersistencePort affiliatePort;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private EntityManager entityManager;

  /** STANDARD tier: 3-month waiting period, so enrolled on 2026-01-15 -> covered from 2026-04-15. */
  private static final LocalDate ENROLLED = LocalDate.of(2026, 1, 15);

  private MembershipHolderEntity holder;

  @BeforeEach
  void seed() {
    jdbcTemplate.update("insert into genders (id, name) values (9301, 'Femenino IT')");
    jdbcTemplate.update("insert into relationships (id, name) values (9301, 'Madre IT')");
    holder =
        holderUseCase.resolveForEnrolment(
            MembershipHolderRequestDto.builder()
                .dni(20444555)
                .firstName("Rosa")
                .lastName("Gomez")
                .build(),
            null);
    final MembershipPrice price = tariff.priceForEnrolment(40, "STANDARD");
    affiliatePort.save(
        AffiliateEntity.builder()
            .dni(AFFILIATE_DNI)
            .firstName("Ana")
            .lastName("Gomez")
            .birthDate(LocalDate.of(1986, 3, 1))
            .deceased(false)
            .gender(entityManager.getReference(GenderEntity.class, 9301L))
            .relationship(entityManager.getReference(RelationshipEntity.class, 9301L))
            .holder(holder)
            .healthTier(price.tier())
            .ageBand(price.band())
            .monthlyFee(price.monthlyFee())
            .feeCalculatedOn(ENROLLED)
            // Back-dated enrolment: the record itself is created today (start_date).
            .enrolmentDate(ENROLLED)
            .build());
    entityManager.flush();
    entityManager.clear();
  }

  private void overdueDues(final int count) {
    final Long affiliateId =
        jdbcTemplate.queryForObject(
            "select id from affiliates where dni = ?", Long.class, AFFILIATE_DNI);
    final Long tierId =
        jdbcTemplate.queryForObject(
            "select id from health_tiers where code = 'STANDARD'", Long.class);
    for (int month = 1; month <= count; month++) {
      jdbcTemplate.update(
          "insert into membership_dues (affiliate_id, holder_id, period, due_date, amount, health_tier_id)"
              + " values (?, ?, ?, ?, 2600.00, ?)",
          affiliateId,
          holder.getId(),
          LocalDate.of(2026, month, 1),
          LocalDate.of(2026, month, 10),
          tierId);
    }
  }

  @Test
  @DisplayName("Given a natural death inside the STANDARD waiting period when evaluating then it is not covered, but a traffic accident is")
  void waitingPeriodAndSeededAccidentalCause() {
    final LocalDate march = LocalDate.of(2026, 3, 1);

    final ClaimEligibilityDto natural = eligibilityUseCase.evaluate(AFFILIATE_DNI, march, SUDDEN_DEATH);
    final ClaimEligibilityDto accident =
        eligibilityUseCase.evaluate(AFFILIATE_DNI, march, TRAFFIC_ACCIDENT);

    assertThat(natural.eligible()).isFalse();
    assertThat(natural.reasons()).containsExactly("WAITING_PERIOD");
    assertThat(natural.coveredFrom()).isEqualTo(LocalDate.of(2026, 4, 15));
    assertThat(accident.eligible()).isTrue();
    assertThat(accident.accidental()).isTrue();
  }

  @Test
  @DisplayName("Given an affiliate saved without an enrolment date then it defaults to the record creation day")
  void enrolmentDateDefaultsToCreationDay() {
    affiliatePort.save(
        AffiliateEntity.builder()
            .dni(AFFILIATE_DNI + 1)
            .firstName("Luis")
            .lastName("Gomez")
            .birthDate(LocalDate.of(1990, 6, 1))
            .deceased(false)
            .gender(entityManager.getReference(GenderEntity.class, 9301L))
            .relationship(entityManager.getReference(RelationshipEntity.class, 9301L))
            .build());
    entityManager.flush();

    final Boolean sameDay =
        jdbcTemplate.queryForObject(
            "select enrolment_date = start_date from affiliates where dni = ?",
            Boolean.class,
            AFFILIATE_DNI + 1);
    final LocalDate seeded =
        jdbcTemplate.queryForObject(
            "select enrolment_date from affiliates where dni = ?", LocalDate.class, AFFILIATE_DNI);

    assertThat(sameDay).isTrue();
    assertThat(seeded).isEqualTo(ENROLLED);
  }

  @Test
  @DisplayName("Given unpaid dues when evaluating after the waiting period then more than the seeded grace (2) is arrears")
  void arrearsAgainstSeededGrace() {
    overdueDues(3);
    final LocalDate june = LocalDate.of(2026, 6, 1);

    final ClaimEligibilityDto threeOverdue = eligibilityUseCase.evaluate(AFFILIATE_DNI, june, null);
    jdbcTemplate.update(
        "update membership_dues set paid_amount = amount where holder_id = ? and period = ?",
        holder.getId(),
        LocalDate.of(2026, 1, 1));
    // The first evaluation loaded the dues into this transaction's persistence context; drop
    // them so the second one reads the JDBC update instead of the cached entities.
    entityManager.clear();
    final ClaimEligibilityDto twoOverdue = eligibilityUseCase.evaluate(AFFILIATE_DNI, june, null);

    assertThat(threeOverdue.overdueGraceCount()).isEqualTo(2);
    assertThat(threeOverdue.overdueCount()).isEqualTo(3);
    assertThat(threeOverdue.reasons()).containsExactly("ACCOUNT_IN_ARREARS");
    assertThat(twoOverdue.overdueCount()).isEqualTo(2);
    assertThat(twoOverdue.eligible()).isTrue();
  }
}
