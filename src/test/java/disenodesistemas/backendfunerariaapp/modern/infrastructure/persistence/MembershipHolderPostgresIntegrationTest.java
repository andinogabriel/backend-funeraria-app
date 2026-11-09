package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipHolderRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderDetailDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderSummaryDto;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Membership holders against a real PostgreSQL (V18): the holder search JPQL (DNI compared as
 * text, case-insensitive names), the holder -> affiliates query, and the fee snapshot columns
 * priced from the seeded V16 tariff. Rolled back per test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class MembershipHolderPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private MembershipHolderUseCase holderUseCase;
  @Autowired private MembershipTariffQueryUseCase tariff;
  @Autowired private AffiliatePersistencePort affiliatePort;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private EntityManager entityManager;

  private MembershipHolderEntity rosa;

  @BeforeEach
  void seed() {
    jdbcTemplate.update("insert into genders (id, name) values (9101, 'Femenino IT')");
    jdbcTemplate.update("insert into relationships (id, name) values (9101, 'Hijo IT')");
    rosa = holderUseCase.resolveForEnrolment(holder(20111222, "Rosa", "Gomez"), null);
    holderUseCase.resolveForEnrolment(holder(27999000, "Pedro", "Alvarez"), null);
  }

  private static MembershipHolderRequestDto holder(
      final int dni, final String firstName, final String lastName) {
    return MembershipHolderRequestDto.builder()
        .dni(dni)
        .firstName(firstName)
        .lastName(lastName)
        .phone("221-555-0000")
        .build();
  }

  private AffiliateEntity enrol(
      final int dni, final LocalDate birthDate, final int age, final String tier) {
    final MembershipPrice price = tariff.priceForEnrolment(age, tier);
    return affiliatePort.save(
        AffiliateEntity.builder()
            .dni(dni)
            .firstName("Afiliado")
            .lastName("Gomez")
            .birthDate(birthDate)
            .deceased(false)
            .gender(entityManager.getReference(GenderEntity.class, 9101L))
            .relationship(entityManager.getReference(RelationshipEntity.class, 9101L))
            .holder(rosa)
            .healthTier(price.tier())
            .ageBand(price.band())
            .monthlyFee(price.monthlyFee())
            .feeCalculatedOn(LocalDate.of(2026, 11, 8))
            .build());
  }

  @Test
  @DisplayName("Given holders when searching by DNI prefix or name fragment then the PG query matches both, case-insensitively")
  void searchByDniPrefixAndName() {
    assertThat(holderUseCase.search("2011"))
        .extracting(s -> s.holder().dni())
        .containsExactly(20111222);
    assertThat(holderUseCase.search("ALVA"))
        .extracting(s -> s.holder().dni())
        .containsExactly(27999000);
    assertThat(holderUseCase.search("zzz")).isEmpty();
  }

  @Test
  @DisplayName("Given a holder covering priced, deceased and deleted affiliates when reading the detail then totals only count alive priced ones")
  void detailTotalsFromSeededTariff() {
    // STANDARD x1.00: age 40 -> 36-50 x1.30 = 2600.00 ; GRADED x1.40: age 10 -> 0-17 x0.50 = 1400.00
    enrol(40000001, LocalDate.of(1986, 5, 1), 40, "STANDARD");
    enrol(40000002, LocalDate.of(2016, 5, 1), 10, "GRADED");
    final AffiliateEntity deceased = enrol(40000003, LocalDate.of(1950, 5, 1), 76, "STANDARD");
    deceased.setDeceased(true);
    final AffiliateEntity deleted = enrol(40000004, LocalDate.of(1990, 5, 1), 36, "STANDARD");
    deleted.setDeletedAt(Instant.parse("2026-11-01T00:00:00Z"));
    entityManager.flush();
    entityManager.clear();

    final MembershipHolderDetailDto detail = holderUseCase.getByDni(20111222);

    assertThat(detail.affiliates())
        .extracting(a -> a.dni())
        .containsExactly(40000001, 40000002, 40000003);
    assertThat(detail.activeAffiliates()).isEqualTo(2);
    assertThat(detail.monthlyTotal()).isEqualByComparingTo("4000.00");
    assertThat(detail.affiliates().getFirst().ageBandLabel()).isEqualTo("36-50");
    assertThat(detail.affiliates().getFirst().healthTier().code()).isEqualTo("STANDARD");
    assertThat(detail.affiliates().getFirst().holder().dni()).isEqualTo(20111222);

    final List<MembershipHolderSummaryDto> summaries = holderUseCase.search("Gomez");
    assertThat(summaries).singleElement().satisfies(
        s -> assertThat(s.monthlyTotal()).isEqualByComparingTo("4000.00"));
  }

  @Test
  @DisplayName("Given an existing holder DNI when enrolling again then the same row is reused and contact data refreshed")
  void reusesHolderByDni() {
    final MembershipHolderEntity again =
        holderUseCase.resolveForEnrolment(
            MembershipHolderRequestDto.builder()
                .dni(20111222)
                .firstName("Rosa Maria")
                .lastName("Gomez")
                .email("rosa@mail.com")
                .build(),
            null);
    entityManager.flush();

    assertThat(again.getId()).isEqualTo(rosa.getId());
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from membership_holders where dni = 20111222", Integer.class))
        .isEqualTo(1);
    assertThat(again.getPhone()).isEqualTo("221-555-0000");
    assertThat(again.getEmail()).isEqualTo("rosa@mail.com");
  }
}
