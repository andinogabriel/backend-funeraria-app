package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.time.LocalDateTime;
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
 * Verifies the per-column filter surface on {@link FuneralPersistencePort#search} against a
 * real PostgreSQL container. Same patterns as the affiliates / incomes / items IT —
 * empty-string sentinels, coalesce date bounds, AND combination, left-join on plan.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FuneralSearchPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final LocalDateTime FD_2026_05_01 = LocalDateTime.parse("2026-05-01T10:00:00");
  private static final LocalDateTime FD_2026_05_15 = LocalDateTime.parse("2026-05-15T11:00:00");
  private static final LocalDateTime FD_2026_06_03 = LocalDateTime.parse("2026-06-03T12:00:00");

  @Autowired private FuneralPersistencePort port;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void seedFixture() {
    jdbcTemplate.update("delete from funeral");
    jdbcTemplate.update("delete from deceased where id >= 9001");
    jdbcTemplate.update("delete from plans where id >= 9001");
    jdbcTemplate.update("delete from genders where id >= 9001");
    jdbcTemplate.update("delete from relationships where id >= 9001");
    jdbcTemplate.update("delete from death_causes where id >= 9001");

    jdbcTemplate.update("insert into genders (id, name) values (9001, 'Masculino')");
    jdbcTemplate.update("insert into genders (id, name) values (9002, 'Femenino')");
    jdbcTemplate.update("insert into relationships (id, name) values (9001, 'Padre')");
    jdbcTemplate.update("insert into death_causes (id, name) values (9001, 'Natural')");

    jdbcTemplate.update(
        "insert into plans (id, name, description, price, profit_percentage) "
            + "values (9001, 'Plan Standard', '', 500000, 30)");
    jdbcTemplate.update(
        "insert into plans (id, name, description, price, profit_percentage) "
            + "values (9002, 'Plan Premium', '', 800000, 30)");

    // Deceased + funeral IDs share the same `>= 9001` range as the seeded lookups so
    // the `delete where id >= 9001` cleanup above wipes everything in one sweep without
    // leaving FK orphans across runs.
    insertDeceased(9101L, 38000001, "Carmen", "Iglesias", 9002L, 9001L, 9001L);
    insertDeceased(9102L, 38000002, "Hector", "Saavedra", 9001L, 9001L, 9001L);
    insertDeceased(9103L, 38000003, "Juan", "Lema", 9001L, 9001L, 9001L);

    insertFuneral(9201L, "F-99001", "A", FD_2026_05_01, 280000, 9101L, 9001L);
    insertFuneral(9202L, "F-99002", "A", FD_2026_05_15, 520000, 9102L, 9001L);
    // Funeral with no plan to exercise the left-join short-circuit.
    insertFuneral(9203L, "F-99003", "A", FD_2026_06_03, 850000, 9103L, null);
  }

  @Test
  @DisplayName(
      "Given a `deceasedName` substring when the search runs then matches against first + last name return")
  void deceasedNameMatchesConcatenatedFirstAndLast() {
    final Page<Funeral> result =
        port.search("saavedra", "", "", "", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(Funeral::getReceiptNumber)
        .containsOnly("F-99002");
  }

  @Test
  @DisplayName("Given a `dni` substring when the search runs then matches against the cast string return")
  void dniMatchesSubstring() {
    final Page<Funeral> result =
        port.search("", "0001", "", "", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(Funeral::getReceiptNumber)
        .containsOnly("F-99001");
  }

  @Test
  @DisplayName(
      "Given a `receiptNumber` substring when the search runs then matching funerals return")
  void receiptNumberMatchesSubstring() {
    final Page<Funeral> result =
        port.search("", "", "9900", "", null, null, defaultPageable());
    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  @DisplayName(
      "Given an exact `planName` filter when the search runs then funerals without a plan are excluded")
  void planNameFiltersExactAndDropsNullPlan() {
    final Page<Funeral> result =
        port.search("", "", "", "Plan Standard", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(Funeral::getReceiptNumber)
        .containsOnly("F-99001", "F-99002");
  }

  @Test
  @DisplayName(
      "Given a from/to window when the search runs then only funerals inside the range are returned")
  void fromToWindowsTheFuneralDateRange() {
    final Page<Funeral> result =
        port.search(
            "",
            "",
            "",
            "",
            LocalDateTime.parse("2026-05-10T00:00:00"),
            LocalDateTime.parse("2026-05-31T23:59:59"),
            defaultPageable());
    assertThat(result.getContent())
        .extracting(Funeral::getReceiptNumber)
        .containsOnly("F-99002");
  }

  @Test
  @DisplayName(
      "Given multiple column filters at once when the search runs then they AND together")
  void multipleFiltersCombineWithAnd() {
    // receiptNumber narrows to all three; planName "Plan Standard" drops F-99003 (no plan);
    // from-bound trims out F-99001 (May 1).
    final Page<Funeral> result =
        port.search(
            "",
            "",
            "F-9900",
            "Plan Standard",
            LocalDateTime.parse("2026-05-10T00:00:00"),
            null,
            defaultPageable());
    assertThat(result.getContent())
        .extracting(Funeral::getReceiptNumber)
        .containsOnly("F-99002");
  }

  @Test
  @DisplayName(
      "Given empty strings on every text filter and null bounds when the search runs then every funeral comes back (including the one with no plan)")
  void noFiltersReturnsEverything() {
    final Page<Funeral> result = port.search("", "", "", "", null, null, defaultPageable());
    assertThat(result.getTotalElements()).isEqualTo(3);
  }

  private void insertDeceased(
      final long id,
      final int dni,
      final String firstName,
      final String lastName,
      final long genderId,
      final long relationshipId,
      final long deathCauseId) {
    jdbcTemplate.update(
        "insert into deceased"
            + " (id, dni, first_name, last_name, birth_date, death_date,"
            + "  gender_id, relationship_id, death_cause_id, affiliated, register_date)"
            + " values (?, ?, ?, ?, '1950-01-01', '2026-05-01',"
            + "  ?, ?, ?, false, now())",
        id,
        dni,
        firstName,
        lastName,
        genderId,
        relationshipId,
        deathCauseId);
  }

  private void insertFuneral(
      final long id,
      final String receiptNumber,
      final String receiptSeries,
      final LocalDateTime funeralDate,
      final int totalAmount,
      final long deceasedId,
      final Long planId) {
    jdbcTemplate.update(
        "insert into funeral"
            + " (id, receipt_number, receipt_series, funeral_date, total_amount, tax,"
            + "  deceased_id, plan_id, register_date)"
            + " values (?, ?, ?, ?, ?, 21, ?, ?, now())",
        id,
        receiptNumber,
        receiptSeries,
        funeralDate,
        totalAmount,
        deceasedId,
        planId);
  }

  private static PageRequest defaultPageable() {
    return PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "funeralDate"));
  }
}
