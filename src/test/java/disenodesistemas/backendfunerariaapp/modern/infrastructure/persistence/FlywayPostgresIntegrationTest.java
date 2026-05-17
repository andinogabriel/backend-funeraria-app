package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FlywayPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName(
      "Given an empty PostgreSQL container when the application starts then Flyway creates the schema and seeds reference catalogs")
  void shouldRunFlywayMigrationsAgainstRealPostgres() {
    final Integer successfulMigrations =
        jdbcTemplate.queryForObject(
            "select count(*) from flyway_schema_history where success = true", Integer.class);
    final Integer roleCount =
        jdbcTemplate.queryForObject("select count(*) from roles", Integer.class);
    final Integer provinceCount =
        jdbcTemplate.queryForObject("select count(*) from provinces", Integer.class);
    final Integer cityCount =
        jdbcTemplate.queryForObject("select count(*) from cities", Integer.class);

    assertThat(successfulMigrations).isEqualTo(5);
    assertThat(roleCount).isEqualTo(2);
    assertThat(provinceCount).isEqualTo(24);
    assertThat(cityCount).isGreaterThan(20);
  }
}
