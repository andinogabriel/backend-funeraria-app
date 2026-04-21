package disenodesistemas.backendfunerariaapp.modern.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the package boundaries agreed for the modular monolith. These guardrails run with the
 * test suite so architectural violations fail fast during local development and CI builds.
 */
@AnalyzeClasses(
    packages = "disenodesistemas.backendfunerariaapp",
    importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureGuardrailsTest {

  @ArchTest
  static final ArchRule domain_must_not_depend_on_outer_layers =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..application..",
              "..infrastructure..",
              "..web..",
              "..mapping..",
              "..persistence.repository..")
          .because("the domain must stay isolated from use cases, HTTP and adapter implementations");

  @ArchTest
  static final ArchRule application_business_code_must_not_depend_on_repositories_or_adapters =
      noClasses()
          .that()
          .resideInAnyPackage(
              "..application.usecase..", "..application.service..", "..application.support..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..", "..persistence.repository..", "..web.controller..")
          .because(
              "application orchestration should depend on ports and DTOs, not repositories or adapter implementations");

  @ArchTest
  static final ArchRule web_controllers_must_not_access_repositories_or_persistence_adapters =
      noClasses()
          .that()
          .resideInAPackage("..web.controller..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..persistence.repository..", "..infrastructure.persistence..")
          .because("controllers should delegate through the application layer instead of talking to data access directly");

  @ArchTest
  static final ArchRule application_and_web_must_not_touch_entity_manager_directly =
      noClasses()
          .that()
          .resideInAnyPackage("..application..", "..web..")
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("jakarta.persistence.EntityManager")
          .because("EntityManager access belongs in infrastructure, not in controllers or use cases");

  @ArchTest
  static final ArchRule application_and_web_must_not_use_security_context_holder_directly =
      noClasses()
          .that()
          .resideInAnyPackage("..application..", "..web..")
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("org.springframework.security.core.context.SecurityContextHolder")
          .because("authenticated user resolution must be abstracted behind the dedicated application port");

  @ArchTest
  static final ArchRule ports_must_be_interfaces =
      classes()
          .that()
          .resideInAPackage("..application.port.out..")
          .should()
          .beInterfaces()
          .because("outbound ports define contracts that infrastructure adapters implement");

  @ArchTest
  static final ArchRule mappers_must_not_depend_on_repositories_or_infrastructure =
      noClasses()
          .that()
          .resideInAPackage("..mapping..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..persistence.repository..", "..infrastructure..", "..web.controller..")
          .because("mappers should stay focused on transformations and not acquire technical responsibilities");
}
