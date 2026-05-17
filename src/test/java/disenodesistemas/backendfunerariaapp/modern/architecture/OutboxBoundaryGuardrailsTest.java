package disenodesistemas.backendfunerariaapp.modern.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;

/**
 * Boundary rules locking the transactional outbox in place (ADR-0013).
 *
 * <p>The intent is that domain events stay pure value objects, the publish call only fires
 * from the application layer (so it can ride the use case's transaction), and the HTTP layer
 * never grows a hard dependency on the event shapes (events are an outbound integration
 * concern, not a wire contract). Future broker integrations will plug in through their own
 * port — direct broker calls bypassing {@link OutboxPort} would defeat the at-least-once
 * delivery guarantee the pattern provides.
 */
@AnalyzeClasses(
    packages = "disenodesistemas.backendfunerariaapp",
    importOptions = {ImportOption.DoNotIncludeTests.class})
class OutboxBoundaryGuardrailsTest {

  @ArchTest
  static final ArchRule only_use_cases_publish_to_the_outbox =
      noClasses()
          .that()
          .resideOutsideOfPackages(
              "..application.usecase..",
              "..application.port..",
              "..infrastructure.outbox..",
              "..infrastructure.persistence..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(OutboxPort.class)
          .because(
              "OutboxPort.publish must run inside a use case transaction so the event commits"
                  + " with the business write; web controllers or infrastructure adapters"
                  + " bypassing the use case layer would write events without that envelope");

  @ArchTest
  static final ArchRule domain_events_must_stay_pure =
      noClasses()
          .that()
          .resideInAPackage("..domain.event..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..application..", "..infrastructure..", "..web..", "..mapping..")
          .because(
              "domain events are pure value objects shared across layers; coupling them to"
                  + " orchestration, adapters or HTTP shapes turns them into a transport"
                  + " contract by accident");

  @ArchTest
  static final ArchRule web_must_not_depend_on_domain_events =
      noClasses()
          .that()
          .resideInAPackage("..web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..domain.event..")
          .because(
              "domain events are an outbound integration concern (consumers of the outbox);"
                  + " the HTTP layer ships request/response DTOs, not event records");
}
