package disenodesistemas.backendfunerariaapp.modern.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AuditEventRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step definitions exercising the affiliate-lifecycle slice of the audit log feature. Shares
 * the {@code an admin "..." is authenticated} step with {@link AuditLogStepDefinitions} —
 * Cucumber's step matcher is global across the glue package, so the Background step resolves
 * to the existing definition without duplication.
 *
 * <p>Affiliate state is cleaned between scenarios so a creation in one scenario does not
 * leak into the next via the shared Spring context (Cucumber-Spring shares a single context
 * for the entire run, per ADR-0012). The DNI values used by the feature file (3XXXXXXX,
 * 4XXXXXXX) deliberately do not collide with the seeded test fixtures.
 */
public class AffiliateLifecycleStepDefinitions {

  /** Reuses the gender/relationship rows seeded by V2 (id 2 = Masculino, id 1 = Padre). */
  private static final GenderDto DEFAULT_GENDER = GenderDto.builder().id(2L).name("Masculino").build();

  private static final RelationshipDto DEFAULT_RELATIONSHIP =
      RelationshipDto.builder().id(1L).name("Padre").build();

  private final AffiliateCommandUseCase affiliateCommandUseCase;
  private final AffiliatePersistencePort affiliatePersistencePort;
  private final AffiliateRepository affiliateRepository;
  private final AuditEventRepository auditEventRepository;
  private final OutboxEventRepository outboxEventRepository;

  @Autowired
  public AffiliateLifecycleStepDefinitions(
      final AffiliateCommandUseCase affiliateCommandUseCase,
      final AffiliatePersistencePort affiliatePersistencePort,
      final AffiliateRepository affiliateRepository,
      final AuditEventRepository auditEventRepository,
      final OutboxEventRepository outboxEventRepository) {
    this.affiliateCommandUseCase = affiliateCommandUseCase;
    this.affiliatePersistencePort = affiliatePersistencePort;
    this.affiliateRepository = affiliateRepository;
    this.auditEventRepository = auditEventRepository;
    this.outboxEventRepository = outboxEventRepository;
  }

  /**
   * Wipes every affiliate, audit event and outbox row before each scenario. Without this the
   * shared Spring context leaks data across scenarios and an affiliate-not-found assertion in
   * the second scenario would be polluted by the first scenario's leftover state.
   */
  @Before
  public void resetAffiliateState() {
    // Order matters: affiliates first so the cascading FK on audit_events / outbox_events
    // does not block the truncate. The repository's `deleteAll` is bulk-friendly under JPA.
    affiliateRepository.deleteAll();
    auditEventRepository.deleteAll();
    outboxEventRepository.deleteAll();
  }

  @Given("an affiliate with dni {int} named {string} {string} already exists")
  public void anAffiliateAlreadyExists(final int dni, final String firstName, final String lastName) {
    affiliateCommandUseCase.create(buildRequest(dni, firstName, lastName));
    // The setup's audit / outbox rows are cleanup of the next assertion; wipe them so the
    // scenario's `Then` clauses only count rows produced by the `When` step under test.
    auditEventRepository.deleteAll();
    outboxEventRepository.deleteAll();
  }

  @When("the admin creates an affiliate with dni {int} named {string} {string}")
  public void theAdminCreatesAnAffiliate(
      final int dni, final String firstName, final String lastName) {
    affiliateCommandUseCase.create(buildRequest(dni, firstName, lastName));
  }

  @When("the admin deletes the affiliate with dni {int}")
  public void theAdminDeletesTheAffiliate(final int dni) {
    affiliateCommandUseCase.delete(dni);
  }

  @When("the admin renames the affiliate with dni {int} to {string} {string}")
  public void theAdminRenamesTheAffiliate(
      final int dni, final String newFirstName, final String newLastName) {
    final var existing = affiliatePersistencePort.findByDni(dni).orElseThrow();
    affiliateCommandUseCase.update(
        dni,
        AffiliateRequestDto.builder()
            .dni(dni)
            .firstName(newFirstName)
            .lastName(newLastName)
            .birthDate(existing.getBirthDate())
            .gender(DEFAULT_GENDER)
            .relationship(DEFAULT_RELATIONSHIP)
            .build());
  }

  @Then("an audit event with action {string} is recorded for affiliate dni {int}")
  public void anAuditEventIsRecordedForAffiliate(final String action, final int dni) {
    final List<AuditEvent> matching = matchingAuditEvents(AuditAction.valueOf(action), dni);
    assertThat(matching).as("%s audit events for dni %d", action, dni).isNotEmpty();
  }

  @Then("the actor on the {word} audit event for dni {int} is {string}")
  public void theActorOnTheAuditEventIs(
      final String action, final int dni, final String expectedEmail) {
    final List<AuditEvent> matching = matchingAuditEvents(AuditAction.valueOf(action), dni);
    assertThat(matching).isNotEmpty();
    assertThat(matching.getLast().getActorEmail()).isEqualTo(expectedEmail);
  }

  @Then("exactly {int} audit event with action {string} exists for affiliate dni {int}")
  public void exactlyNAuditEventsExistForAffiliate(
      final int expectedCount, final String action, final int dni) {
    final List<AuditEvent> matching = matchingAuditEvents(AuditAction.valueOf(action), dni);
    assertThat(matching).hasSize(expectedCount);
  }

  private AffiliateRequestDto buildRequest(
      final int dni, final String firstName, final String lastName) {
    return AffiliateRequestDto.builder()
        .dni(dni)
        .firstName(firstName)
        .lastName(lastName)
        .birthDate(LocalDate.of(1980, 1, 1))
        .gender(DEFAULT_GENDER)
        .relationship(DEFAULT_RELATIONSHIP)
        .build();
  }

  private List<AuditEvent> matchingAuditEvents(final AuditAction action, final int dni) {
    return auditEventRepository.findAll().stream()
        .filter(e -> e.getAction() == action)
        .filter(e -> String.valueOf(dni).equals(e.getTargetId()))
        .toList();
  }
}
