package disenodesistemas.backendfunerariaapp.modern.support;

import disenodesistemas.backendfunerariaapp.application.port.out.FileStoragePort;
import org.flywaydb.core.Flyway;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Provides a shared PostgreSQL Testcontainers setup for integration tests that need the real
 * database engine together with Flyway migrations and Spring Boot wiring.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractPostgresIntegrationTest {

  @MockitoBean protected FileStoragePort fileStoragePort;

  @Container
  @SuppressWarnings("resource")
  protected static final PostgreSQLContainer<?> POSTGRESQL =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("funerariadb")
          .withUsername("postgres")
          .withPassword("postgres");

  private static void ensureContainerAndSchemaReady() {
    if (!POSTGRESQL.isRunning()) {
      POSTGRESQL.start();
    }

    // Migrate explicitly before Spring builds the EntityManagerFactory so Hibernate validation
    // always sees the real schema, including on slower CI bootstraps.
    Flyway.configure()
        .dataSource(POSTGRESQL.getJdbcUrl(), POSTGRESQL.getUsername(), POSTGRESQL.getPassword())
        .locations("classpath:db/migration")
        .cleanDisabled(true)
        .load()
        .migrate();
  }

  /**
   * Registers the dynamic properties required for Spring Boot to use the running PostgreSQL
   * container together with the non-database defaults needed by the application during
   * integration testing.
   */
  @DynamicPropertySource
  static void registerPostgresProperties(final DynamicPropertyRegistry registry) {
    ensureContainerAndSchemaReady();
    registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    registry.add("spring.jpa.show-sql", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "false");
    registry.add("spring.sql.init.mode", () -> "never");
    registry.add("app.storage.provider", () -> "local");
    registry.add("app.storage.local.root-path", () -> "target/test-storage");
    registry.add("app.storage.local.public-base-url", () -> "http://localhost:8081/files/");
  }
}
