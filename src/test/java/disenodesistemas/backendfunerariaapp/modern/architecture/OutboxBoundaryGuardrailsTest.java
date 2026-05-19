package disenodesistemas.backendfunerariaapp.modern.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import disenodesistemas.backendfunerariaapp.application.port.out.ActivityLogRetentionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.DomainEventConsumer;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxRetentionPort;

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

  /**
   * ADR-0014: every {@link DomainEventConsumer} implementation must live under
   * {@code ..infrastructure.outbox.consumer..}. The relay (in
   * {@code ..infrastructure.outbox..}) injects {@code List<DomainEventConsumer>}, so a
   * consumer dropped anywhere on the classpath would silently be picked up — keeping them
   * collocated makes the fan-out roster easy to audit.
   */
  @ArchTest
  static final ArchRule consumers_must_live_under_outbox_consumer_package =
      classes()
          .that()
          .implement(DomainEventConsumer.class)
          .should()
          .resideInAPackage("..infrastructure.outbox.consumer..")
          .because(
              "the relay's fan-out roster must be one greppable folder; consumers leaking"
                  + " into feature packages would be picked up by Spring's autowiring without"
                  + " any architectural decision");

  /**
   * The consumer package is an implementation detail of the outbox; nothing outside the
   * outbox itself should depend on the concrete consumer classes (they are addressed through
   * the {@link DomainEventConsumer} port). This rule catches a feature accidentally calling
   * {@code ActivityLogConsumer.consume} directly, which would bypass the relay's failure
   * isolation and the activity-log's idempotency contract.
   */
  @ArchTest
  static final ArchRule consumers_must_not_be_referenced_outside_outbox =
      noClasses()
          .that()
          .resideOutsideOfPackage("..infrastructure.outbox..")
          .and()
          .resideOutsideOfPackage(
              "..modern.infrastructure.outbox..") // tests under the same package mirror
          .and()
          .resideOutsideOfPackage("..modern.infrastructure.persistence..") // ITs
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure.outbox.consumer..")
          .because(
              "consumers are an outbox-internal detail; call sites must talk to the relay or"
                  + " the read model they project to, not the consumer class directly");

  /**
   * ADR-0015: the retention scheduler lives in {@code ..infrastructure.retention..} so the
   * cron concern is collocated with the rest of the periodic-job infrastructure. The
   * use case + ports live in the application layer; the scheduler is the only piece
   * that actually wears the {@code @Scheduled} annotation.
   */
  @ArchTest
  static final ArchRule retention_scheduler_must_live_under_infrastructure_retention =
      classes()
          .that()
          .haveSimpleNameEndingWith("Scheduler")
          .and()
          .resideInAPackage("..backendfunerariaapp..")
          .and()
          .areNotInterfaces()
          .should()
          .resideInAPackage("..infrastructure..")
          .because(
              "schedulers are an infrastructure concern; the use case stays free of cron"
                  + " annotations so it can be exercised by a unit test without Spring");

  /**
   * Retention ports must remain pure outbound contracts — only application use cases and
   * the JPA adapters that implement them may depend on the port types. Lets ArchUnit catch
   * a future controller or scheduler accidentally short-circuiting the use case.
   */
  @ArchTest
  static final ArchRule retention_ports_must_only_be_used_by_use_cases_and_adapters =
      noClasses()
          .that()
          .resideOutsideOfPackages(
              "..application.usecase.retention..",
              "..application.port..",
              "..infrastructure.persistence..",
              "..modern..") // tests
          .should()
          .dependOnClassesThat()
          .areAssignableTo(OutboxRetentionPort.class)
          .orShould()
          .dependOnClassesThat()
          .areAssignableTo(ActivityLogRetentionPort.class)
          .because(
              "the retention sweep is owned by the application's RetentionUseCase; HTTP"
                  + " controllers, web filters or other features bypassing the use case"
                  + " would break the per-batch isolation the use case wraps");
}
