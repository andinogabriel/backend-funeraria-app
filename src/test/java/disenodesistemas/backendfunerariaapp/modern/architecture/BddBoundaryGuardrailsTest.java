package disenodesistemas.backendfunerariaapp.modern.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architectural guardrail for the Cucumber BDD slice. Scoped to the test classpath because the
 * production guardrails (in {@link ArchitectureGuardrailsTest}) explicitly exclude tests; this
 * class enables full-classpath analysis specifically to enforce that step definitions, the
 * Spring configuration anchor and the suite runner do not leak Cucumber dependencies into
 * neighboring test packages.
 *
 * <p>Keeping every {@code io.cucumber} dependency under {@code ..bdd..} matters for two
 * reasons: Cucumber-Spring rejects more than one {@code @CucumberContextConfiguration} class
 * on the classpath, and concentrating glue makes it trivial to grep the suite for what runs
 * under Cucumber versus the JUnit-only tests.
 */
@AnalyzeClasses(packages = "disenodesistemas.backendfunerariaapp")
class BddBoundaryGuardrailsTest {

  @ArchTest
  static final ArchRule cucumber_glue_must_stay_under_bdd_package =
      noClasses()
          .that()
          .resideOutsideOfPackage("..bdd..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("io.cucumber..")
          .because(
              "Cucumber step definitions and the @CucumberContextConfiguration anchor must "
                  + "live under the dedicated 'bdd' package; spreading glue across the codebase "
                  + "breaks Cucumber's single-context rule and pollutes the rest of the test "
                  + "suite with unrelated framework imports");
}
