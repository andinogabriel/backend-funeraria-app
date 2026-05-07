package disenodesistemas.backendfunerariaapp.modern.bdd;

import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Single Spring configuration anchor used by Cucumber. {@link CucumberContextConfiguration}
 * tells the Cucumber-Spring glue which Spring Boot context to bootstrap for every scenario,
 * and inheriting from {@link AbstractPostgresIntegrationTest} reuses the existing
 * Testcontainers PostgreSQL fixture so BDD scenarios run against the real database engine
 * with the actual Flyway migrations applied — exactly the same surface the rest of the
 * integration suite covers.
 *
 * <p>Cucumber rejects multiple {@code @CucumberContextConfiguration} classes on the
 * classpath; keep this as the single anchor and add new step-definition classes as plain
 * Spring components instead of duplicating the annotation here.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration extends AbstractPostgresIntegrationTest {}
