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
              "..application..", "..infrastructure..", "..web..", "..mapping..")
          .because("the domain must stay isolated from use cases, HTTP and adapter implementations");

  @ArchTest
  static final ArchRule application_business_code_must_not_depend_on_repositories_or_adapters =
      noClasses()
          .that()
          .resideInAnyPackage("..application.usecase..", "..application.support..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..", "..web.controller..")
          .because(
              "application orchestration should depend on ports and DTOs, not repositories or adapter implementations");

  @ArchTest
  static final ArchRule legacy_application_service_facade_must_stay_empty =
      noClasses()
          .should()
          .resideInAPackage("..application.service..")
          .because(
              "the 'application.service' pass-through facade was removed; controllers depend on "
                  + "command and query use cases directly, and this package must not return");

  @ArchTest
  static final ArchRule web_controllers_must_not_access_repositories_or_persistence_adapters =
      noClasses()
          .that()
          .resideInAPackage("..web.controller..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure.persistence..")
          .because("controllers should delegate through the application layer instead of talking to data access directly");

  @ArchTest
  static final ArchRule jpa_repositories_must_live_in_infrastructure =
      classes()
          .that()
          .areAssignableTo(org.springframework.data.repository.Repository.class)
          .and()
          .areInterfaces()
          .should()
          .resideInAPackage("..infrastructure.persistence.repository..")
          .because(
              "JPA repositories are an infrastructure detail and must not live anywhere else "
                  + "(prevents regressions where new repositories are added under a top-level "
                  + "persistence/repository package by mistake)");

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
          .resideInAnyPackage("..infrastructure..", "..web.controller..")
          .because("mappers should stay focused on transformations and not acquire technical responsibilities");

  @ArchTest
  static final ArchRule legacy_top_level_security_package_must_stay_empty =
      noClasses()
          .should()
          .resideInAPackage("disenodesistemas.backendfunerariaapp.security..")
          .because(
              "security adapters and properties live under 'infrastructure.security' and Bean "
                  + "Validation annotations under 'web.dto.validation'; the legacy top-level "
                  + "'security' package was consolidated and must not return");
}
