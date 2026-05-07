package disenodesistemas.backendfunerariaapp.modern.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit 5 entry point that wires Surefire into the Cucumber engine. Discovery rules live
 * entirely in {@code junit-platform.properties} and the annotations below, so adding a new
 * feature only requires dropping a {@code .feature} file under
 * {@code src/test/resources/features} — no new runner class is needed.
 *
 * <p>This suite is excluded by default from the Surefire pattern in {@code pom.xml} and
 * runs only under the Maven {@code bdd} profile (CI invokes {@code mvn -Pbdd verify}). The
 * underlying {@link disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest}
 * fixture requires a PostgreSQL Testcontainer, and unlike JUnit Jupiter integration tests
 * the Cucumber engine bootstraps its Spring context outside the lifecycle that honors
 * {@code @Testcontainers(disabledWithoutDocker = true)}; gating the suite at the build
 * level keeps a Docker-less local {@code mvn verify} green.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = "cucumber.glue",
    value = "disenodesistemas.backendfunerariaapp.modern.bdd")
class CucumberSuiteTest {}
