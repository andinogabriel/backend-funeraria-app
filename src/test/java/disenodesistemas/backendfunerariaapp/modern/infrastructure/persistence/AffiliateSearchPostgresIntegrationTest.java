package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Verifies the per-column filter surface on {@link AffiliatePersistencePort#search} against a
 * real PostgreSQL container. Mirrors the structure of {@code IncomeSearchPostgresIntegrationTest}
 * so the same patterns (empty-string sentinels, coalesce date bounds, AND combination) read
 * the same way across both query surfaces.
 *
 * <p>The fixture is built with {@code JdbcTemplate} instead of the JPA repositories so the test
 * stays decoupled from the mapper code, and the seeded relationships exercise the left-join
 * behaviour around the optional {@code relationship_id}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AffiliateSearchPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final LocalDate B_1990_01_15 = LocalDate.parse("1990-01-15");
  private static final LocalDate B_1995_06_20 = LocalDate.parse("1995-06-20");
  private static final LocalDate B_2000_12_30 = LocalDate.parse("2000-12-30");
  private static final LocalDate B_1975_03_10 = LocalDate.parse("1975-03-10");

  @Autowired private AffiliatePersistencePort port;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void seedFixture() {
    // Clean the rows we touch — leave genders / relationships seeded from V1 alone so the
    // FK constraints stay satisfied. We only reset affiliates between tests.
    jdbcTemplate.update("delete from affiliates");
    jdbcTemplate.update("delete from relationships where id >= 9001");
    jdbcTemplate.update("delete from genders where id >= 9001");

    jdbcTemplate.update("insert into genders (id, name) values (9001, 'Masculino')");
    jdbcTemplate.update("insert into genders (id, name) values (9002, 'Femenino')");
    jdbcTemplate.update("insert into relationships (id, name) values (9001, 'Padre')");
    jdbcTemplate.update("insert into relationships (id, name) values (9002, 'Madre')");
    jdbcTemplate.update("insert into relationships (id, name) values (9003, 'Hijo')");

    insertAffiliate(8001L, 35000001, "Mariana", "Quiroga", B_1990_01_15, 9002, 9002, false);
    insertAffiliate(8002L, 35000002, "Ricardo", "Ferreyra", B_1975_03_10, 9001, 9001, false);
    insertAffiliate(8003L, 35000003, "Sofia", "Vazquez", B_1995_06_20, 9002, 9003, false);
    insertAffiliate(8004L, 35000004, "Tomas", "Vazquez", B_2000_12_30, 9001, 9003, false);
    insertAffiliate(8005L, 35000099, "Carlos", "Deceased", B_1995_06_20, 9001, 9001, true);
  }

  @Test
  @DisplayName(
      "Given a `firstName` substring when the search runs then only affiliates whose first name contains the value are returned")
  void firstNameMatchesSubstring() {
    final Page<AffiliateEntity> result =
        port.search(false, "mar", "", "", "", null, null, defaultPageable());
    assertThat(result.getContent()).extracting(AffiliateEntity::getDni).containsOnly(35000001);
  }

  @Test
  @DisplayName(
      "Given a `lastName` substring when the search runs then only affiliates whose last name contains the value are returned")
  void lastNameMatchesSubstring() {
    final Page<AffiliateEntity> result =
        port.search(false, "", "vaz", "", "", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(AffiliateEntity::getDni)
        .containsOnly(35000003, 35000004);
  }

  @Test
  @DisplayName(
      "Given a `dni` substring when the search runs then only affiliates whose DNI cast to string contains the value are returned")
  void dniMatchesSubstring() {
    final Page<AffiliateEntity> result =
        port.search(false, "", "", "0001", "", null, null, defaultPageable());
    assertThat(result.getContent()).extracting(AffiliateEntity::getDni).containsOnly(35000001);
  }

  @Test
  @DisplayName(
      "Given an exact `relationshipName` filter when the search runs then only affiliates with that relationship come back")
  void relationshipNameFiltersExact() {
    final Page<AffiliateEntity> result =
        port.search(false, "", "", "", "Hijo", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(AffiliateEntity::getDni)
        .containsOnly(35000003, 35000004);
  }

  @Test
  @DisplayName(
      "Given a from/to window when the search runs then only affiliates inside the range are returned")
  void fromToWindowsTheBirthDateRange() {
    final Page<AffiliateEntity> result =
        port.search(
            false,
            "",
            "",
            "",
            "",
            LocalDate.parse("1990-01-01"),
            LocalDate.parse("1999-12-31"),
            defaultPageable());
    assertThat(result.getContent())
        .extracting(AffiliateEntity::getDni)
        .containsOnly(35000001, 35000003);
  }

  @Test
  @DisplayName(
      "Given multiple column filters at once when the search runs then they AND together")
  void multipleFiltersCombineWithAnd() {
    // lastName "vaz" narrows to Sofía + Tomás; relationship Hijo keeps both; from 2000-01-01
    // trims out Sofía (1995) leaving Tomás (2000).
    final Page<AffiliateEntity> result =
        port.search(
            false,
            "",
            "vaz",
            "",
            "Hijo",
            LocalDate.parse("2000-01-01"),
            null,
            defaultPageable());
    assertThat(result.getContent()).extracting(AffiliateEntity::getDni).containsOnly(35000004);
  }

  @Test
  @DisplayName(
      "Given the `deceased` flag set to false when the search runs then deceased rows are excluded")
  void deceasedFlagExcludesDeceasedRows() {
    final Page<AffiliateEntity> result =
        port.search(false, "", "", "", "", null, null, defaultPageable());
    assertThat(result.getTotalElements()).isEqualTo(4);
    assertThat(result.getContent())
        .extracting(AffiliateEntity::getDni)
        .doesNotContain(35000099);
  }

  @Test
  @DisplayName(
      "Given empty strings on every text filter and null bounds when the search runs then every active affiliate comes back")
  void noFiltersReturnsEveryActiveAffiliate() {
    final Page<AffiliateEntity> result =
        port.search(false, "", "", "", "", null, null, defaultPageable());
    assertThat(result.getTotalElements()).isEqualTo(4);
  }

  /**
   * Inserts a minimal affiliate row using JdbcTemplate so the test stays decoupled from the
   * JPA mapper. Sets the columns the search query reads from — start_date defaults to the
   * insert moment via the auditing listener.
   */
  private void insertAffiliate(
      final long id,
      final int dni,
      final String firstName,
      final String lastName,
      final LocalDate birthDate,
      final long genderId,
      final long relationshipId,
      final boolean deceased) {
    jdbcTemplate.update(
        "insert into affiliates"
            + " (id, dni, first_name, last_name, birth_date, gender_id, relationship_id, deceased, start_date)"
            + " values (?, ?, ?, ?, ?, ?, ?, ?, current_date)",
        id,
        dni,
        firstName,
        lastName,
        birthDate,
        genderId,
        relationshipId,
        deceased);
  }

  private static PageRequest defaultPageable() {
    return PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "lastName"));
  }
}
