package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.IncomePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
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
 * Verifies the per-column filter surface on {@link IncomePersistencePort#search} against a
 * real PostgreSQL container. The filters are independent (AND semantics, no multi-purpose
 * fallback) so each test pins one filter at a time and asserts the result set shape.
 *
 * <p>The fixture is built with `JdbcTemplate` instead of the JPA repositories so the test
 * stays decoupled from the mapper code, and the same setup exercises the JPQL query's
 * left-join behaviour around suppliers — the income with {@code supplier_id = null} is
 * still matchable by the {@code receiptNumber} predicate but never by {@code supplierNif}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class IncomeSearchPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final LocalDateTime T_2026_05_01 = LocalDateTime.parse("2026-05-01T10:00:00");
  private static final LocalDateTime T_2026_05_15 = LocalDateTime.parse("2026-05-15T11:00:00");
  private static final LocalDateTime T_2026_06_03 = LocalDateTime.parse("2026-06-03T12:00:00");

  @Autowired private IncomePersistencePort port;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void seedFixture() {
    jdbcTemplate.update("delete from income_details");
    jdbcTemplate.update("delete from incomes");
    jdbcTemplate.update("delete from suppliers");

    // Two suppliers, one will be left dangling on income #4 (null supplier_id).
    jdbcTemplate.update(
        "insert into suppliers (id, nif, email, web_page, name) "
            + "values (101, '30-11111111-1', 'acme@example.com', null, 'ACME Mayorista')");
    jdbcTemplate.update(
        "insert into suppliers (id, nif, email, web_page, name) "
            + "values (102, '30-22222222-2', 'flores@example.com', null, 'Flores del Sur')");

    insertIncome(1, 1001L, T_2026_05_01, 101);
    insertIncome(2, 1002L, T_2026_05_15, 102);
    insertIncome(3, 1003L, T_2026_06_03, 101);
    insertIncome(4, 99001L, T_2026_06_03, null); // no supplier, only matchable by receipt
  }

  @Test
  @DisplayName(
      "Given a `receiptNumber` substring when the search runs then only incomes whose receipt contains the value are returned (including those without a supplier)")
  void receiptNumberMatchesSubstring() {
    final Page<IncomeEntity> result =
        port.search(false, "9900", "", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(IncomeEntity::getReceiptNumber)
        .containsOnly(99001L);
  }

  @Test
  @DisplayName(
      "Given a `receiptNumber` substring that matches more than one row when the search runs then every match is returned")
  void receiptNumberMatchesMultiple() {
    final Page<IncomeEntity> result =
        port.search(false, "100", "", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(IncomeEntity::getReceiptNumber)
        .containsOnly(1001L, 1002L, 1003L);
  }

  @Test
  @DisplayName(
      "Given a `receiptNumber` value that looks like a supplier name when the search runs then it does NOT match suppliers (per-column scoping)")
  void receiptNumberDoesNotMatchSupplierName() {
    // 'acme' was the old multi-purpose `q` match against supplier.name; the new
    // receiptNumber filter is column-scoped, so a substring that matches a supplier name
    // but not any receipt must return nothing.
    final Page<IncomeEntity> result =
        port.search(false, "acme", "", null, null, defaultPageable());
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName(
      "Given an exact `supplierNif` filter when the search runs then only incomes for that supplier come back")
  void supplierNifFiltersExact() {
    final Page<IncomeEntity> result =
        port.search(false, "", "30-11111111-1", null, null, defaultPageable());
    assertThat(result.getContent())
        .extracting(IncomeEntity::getReceiptNumber)
        .containsOnly(1001L, 1003L);
  }

  @Test
  @DisplayName(
      "Given a from/to window when the search runs then only incomes inside the range are returned")
  void fromToWindowsTheIncomeDateRange() {
    final Page<IncomeEntity> result =
        port.search(
            false,
            "",
            "",
            LocalDateTime.parse("2026-05-10T00:00:00"),
            LocalDateTime.parse("2026-05-31T23:59:59"),
            defaultPageable());
    assertThat(result.getContent())
        .extracting(IncomeEntity::getReceiptNumber)
        .containsOnly(1002L);
  }

  @Test
  @DisplayName(
      "Given multiple column filters at once when the search runs then they AND together")
  void multipleFiltersCombineWithAnd() {
    // receiptNumber narrows to the two ACME receipts (1001 + 1003); the from-bound trims
    // out 1001 (May 1) and leaves 1003 (Jun 3).
    final Page<IncomeEntity> result =
        port.search(
            false,
            "100",
            "30-11111111-1",
            LocalDateTime.parse("2026-06-01T00:00:00"),
            null,
            defaultPageable());
    assertThat(result.getContent())
        .extracting(IncomeEntity::getReceiptNumber)
        .containsOnly(1003L);
  }

  @Test
  @DisplayName(
      "Given empty strings on every text filter and null bounds when the search runs then every non-deleted income comes back")
  void noFiltersReturnsEverything() {
    final Page<IncomeEntity> result = port.search(false, "", "", null, null, defaultPageable());
    assertThat(result.getTotalElements()).isEqualTo(4);
  }

  /**
   * Inserts a minimal income row using JdbcTemplate so the test stays decoupled from the JPA
   * mapper. Sets the columns the search query reads from — every other column is either
   * nullable or has a DB-side default.
   */
  private void insertIncome(
      final long id,
      final long receiptNumber,
      final LocalDateTime incomeDate,
      final Integer supplierId) {
    jdbcTemplate.update(
        "insert into incomes (id, deleted, tax, total_amount, income_date, receipt_number,"
            + " receipt_series, supplier_id)"
            + " values (?, false, 21, 0, ?, ?, 1, ?)",
        id,
        incomeDate,
        receiptNumber,
        supplierId);
  }

  private static PageRequest defaultPageable() {
    return PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "incomeDate"));
  }
}
