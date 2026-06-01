package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
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
 * Verifies the per-column filter surface on {@link ItemPersistencePort#search} against a
 * real PostgreSQL container. Same patterns as the affiliates / incomes IT — empty-string
 * sentinels, AND combination, left-join behaviour around optional FKs (category, brand).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ItemSearchPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private ItemPersistencePort port;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void seedFixture() {
    jdbcTemplate.update("delete from items_plan");
    jdbcTemplate.update("delete from income_details");
    jdbcTemplate.update("delete from items");
    jdbcTemplate.update("delete from brands where id >= 9001");
    jdbcTemplate.update("delete from categories where id >= 9001");

    jdbcTemplate.update("insert into categories (id, name) values (9001, 'Cofres')");
    jdbcTemplate.update("insert into categories (id, name) values (9002, 'Velas')");
    jdbcTemplate.update(
        "insert into brands (id, name, web_page) values (9001, 'Akme', null)");
    jdbcTemplate.update(
        "insert into brands (id, name, web_page) values (9002, 'Roble Norte', null)");

    insertItem(8001L, "COF-001", "Cofre Standard", 150000, 9001L, 9001L);
    insertItem(8002L, "COF-002", "Cofre Premium", 320000, 9001L, 9002L);
    insertItem(8003L, "VEL-001", "Vela 30cm", 950, 9002L, 9001L);
    // Item with no brand to exercise the left-join short-circuit.
    insertItem(8004L, "FLO-001", "Flor especial", 22000, null, null);
  }

  @Test
  @DisplayName("Given a `code` substring when the search runs then only matching codes return")
  void codeMatchesSubstring() {
    final Page<ItemEntity> result = port.search("COF", "", "", "", false, defaultPageable());
    assertThat(result.getContent()).extracting(ItemEntity::getCode).containsOnly("COF-001", "COF-002");
  }

  @Test
  @DisplayName("Given a `name` substring when the search runs then only matching names return")
  void nameMatchesSubstring() {
    final Page<ItemEntity> result = port.search("", "Cofre", "", "", false, defaultPageable());
    assertThat(result.getContent()).extracting(ItemEntity::getCode).containsOnly("COF-001", "COF-002");
  }

  @Test
  @DisplayName(
      "Given an exact `categoryName` filter when the search runs then only matching items return")
  void categoryNameFiltersExact() {
    final Page<ItemEntity> result = port.search("", "", "Velas", "", false, defaultPageable());
    assertThat(result.getContent()).extracting(ItemEntity::getCode).containsOnly("VEL-001");
  }

  @Test
  @DisplayName(
      "Given an exact `brandName` filter when the search runs then items without a brand are excluded")
  void brandNameFiltersExactAndDropsNullBrand() {
    final Page<ItemEntity> result = port.search("", "", "", "Akme", false, defaultPageable());
    assertThat(result.getContent())
        .extracting(ItemEntity::getCode)
        .containsOnly("COF-001", "VEL-001");
  }

  @Test
  @DisplayName(
      "Given multiple column filters at once when the search runs then they AND together")
  void multipleFiltersCombineWithAnd() {
    final Page<ItemEntity> result =
        port.search("COF", "", "Cofres", "Akme", false, defaultPageable());
    assertThat(result.getContent()).extracting(ItemEntity::getCode).containsOnly("COF-001");
  }

  @Test
  @DisplayName(
      "Given empty strings on every filter when the search runs then every item comes back (including the one with no brand)")
  void noFiltersReturnsEverything() {
    final Page<ItemEntity> result = port.search("", "", "", "", false, defaultPageable());
    assertThat(result.getTotalElements()).isEqualTo(4);
  }

  @Test
  @DisplayName(
      "Given lowStock=true when the search runs then only items at or below their threshold (with a non-null stock) return")
  void lowStockFiltersBelowThreshold() {
    // The fixture items (8001..8004) have a null stock (default), so they are never "low" and
    // must be excluded by the lowStock predicate; only the three explicit-stock rows qualify.
    insertItemWithStock(8101L, "STK-LOW", "Stock bajo", 2, 5);
    insertItemWithStock(8102L, "STK-EQ", "Stock al umbral", 5, 5);
    insertItemWithStock(8103L, "STK-OK", "Stock sobrado", 20, 5);

    final Page<ItemEntity> result = port.search("", "", "", "", true, defaultPageable());
    assertThat(result.getContent())
        .extracting(ItemEntity::getCode)
        .containsExactlyInAnyOrder("STK-LOW", "STK-EQ");
  }

  private void insertItem(
      final long id,
      final String code,
      final String name,
      final int price,
      final Long categoryId,
      final Long brandId) {
    jdbcTemplate.update(
        "insert into items"
            + " (id, code, name, price, category_id, brand_id, created_at, created_by, updated_at, updated_by)"
            + " values (?, ?, ?, ?, ?, ?, now(), 'test', now(), 'test')",
        id,
        code,
        name,
        price,
        categoryId,
        brandId);
  }

  private void insertItemWithStock(
      final long id, final String code, final String name, final int stock, final int threshold) {
    jdbcTemplate.update(
        "insert into items"
            + " (id, code, name, price, stock, low_stock_threshold,"
            + " created_at, created_by, updated_at, updated_by)"
            + " values (?, ?, ?, 1000, ?, ?, now(), 'test', now(), 'test')",
        id,
        code,
        name,
        stock,
        threshold);
  }

  private static PageRequest defaultPageable() {
    return PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name"));
  }
}
