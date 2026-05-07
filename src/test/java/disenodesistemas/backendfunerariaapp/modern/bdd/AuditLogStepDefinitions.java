package disenodesistemas.backendfunerariaapp.modern.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AuditEventRepository;
import disenodesistemas.backendfunerariaapp.web.dto.request.RolRequestDto;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Step definitions for the audit log feature. Each step exercises the real application stack
 * (use cases on top of the JPA adapters running against PostgreSQL) so the scenarios verify
 * the same surface a user-facing call would touch — no test-only shortcuts.
 *
 * <p>Authentication is set on the SecurityContext directly because the audit log emitter
 * resolves the actor through {@code AuthenticatedUserPort}, which reads the principal name
 * from Spring Security; bypassing the HTTP layer keeps the scenarios focused on the audit
 * behavior without re-asserting login plumbing already covered by the integration tests.
 */
public class AuditLogStepDefinitions {

  private static final String ADMIN_PASSWORD_HASH = "{noop}bdd-admin-bypass";
  private static final String AUDITEE_PASSWORD_HASH = "{noop}bdd-auditee-bypass";

  private final UserPersistencePort userPersistencePort;
  private final RolePersistencePort rolePersistencePort;
  private final UserRoleUseCase userRoleUseCase;
  private final AuditEventRepository auditEventRepository;

  @Autowired
  public AuditLogStepDefinitions(
      final UserPersistencePort userPersistencePort,
      final RolePersistencePort rolePersistencePort,
      final UserRoleUseCase userRoleUseCase,
      final AuditEventRepository auditEventRepository) {
    this.userPersistencePort = userPersistencePort;
    this.rolePersistencePort = rolePersistencePort;
    this.userRoleUseCase = userRoleUseCase;
    this.auditEventRepository = auditEventRepository;
  }

  /**
   * Resets the audit log and clears any leftover Spring Security context so scenarios start
   * from a known state. Cucumber-Spring shares one Spring context across the run, so the
   * background must explicitly clean what it relies on.
   */
  @Before
  public void resetAuditState() {
    auditEventRepository.deleteAll();
    SecurityContextHolder.clearContext();
  }

  @Given("an admin {string} is authenticated")
  public void anAdminIsAuthenticated(final String email) {
    final UserEntity admin = ensureUserWithRole(email, "BDD", "Admin", ADMIN_PASSWORD_HASH, Role.ROLE_ADMIN);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new TestingAuthenticationToken(admin.getEmail(), null, "ROLE_ADMIN"));
  }

  @Given("a regular user {string} exists in the system")
  public void aRegularUserExists(final String email) {
    ensureUserWithRole(email, "BDD", "Auditee", AUDITEE_PASSWORD_HASH, Role.ROLE_USER);
  }

  @When("the admin grants the {string} role to {string}")
  public void theAdminGrantsTheRoleTo(final String roleName, final String email) {
    userRoleUseCase.updateUserRol(
        email, RolRequestDto.builder().name(Role.valueOf(roleName)).build());
  }

  @Then("an audit event with action {string} is recorded for {string}")
  public void anAuditEventIsRecordedFor(final String action, final String targetEmail) {
    final UserEntity target =
        userPersistencePort
            .findByEmail(targetEmail)
            .orElseThrow(() -> new AssertionError("expected user not found: " + targetEmail));
    final List<AuditEvent> events = matchingEvents(AuditAction.valueOf(action), target.getId());
    assertThat(events).as("audit events for %s", targetEmail).isNotEmpty();
  }

  @Then("the audit event payload references the granted role {string}")
  public void theAuditEventPayloadReferencesTheRole(final String roleName) {
    final List<AuditEvent> events = auditEventRepository.findAll();
    assertThat(events).isNotEmpty();
    final AuditEvent latest = events.getLast();
    assertThat(latest.getPayload())
        .as("audit payload should mention the granted role")
        .contains(roleName);
  }

  @Then("exactly {int} audit event with action {string} exists for {string}")
  public void exactlyNAuditEventsExistFor(
      final int expectedCount, final String action, final String targetEmail) {
    final UserEntity target =
        userPersistencePort
            .findByEmail(targetEmail)
            .orElseThrow(() -> new AssertionError("expected user not found: " + targetEmail));
    final List<AuditEvent> events = matchingEvents(AuditAction.valueOf(action), target.getId());
    assertThat(events).hasSize(expectedCount);
  }

  /**
   * Creates the user when missing and ensures the supplied role is attached. Idempotent so
   * scenarios that share a {@code Background} can run in any order without colliding.
   */
  private UserEntity ensureUserWithRole(
      final String email,
      final String firstName,
      final String lastName,
      final String passwordHash,
      final Role roleName) {
    final UserEntity user =
        userPersistencePort
            .findByEmail(email)
            .orElseGet(
                () -> {
                  final UserEntity created =
                      new UserEntity(email, firstName, lastName, passwordHash);
                  created.activate();
                  created.setEnabled(true);
                  return userPersistencePort.save(created);
                });
    final RoleEntity role =
        rolePersistencePort
            .findByName(roleName)
            .orElseThrow(() -> new AssertionError("expected role not seeded: " + roleName));
    if (user.getRoles().stream().noneMatch(r -> r.getId().equals(role.getId()))) {
      user.addRol(role);
      userPersistencePort.save(user);
    }
    return user;
  }

  private List<AuditEvent> matchingEvents(final AuditAction action, final Long targetId) {
    return auditEventRepository.findAll().stream()
        .filter(e -> e.getAction() == action)
        .filter(e -> String.valueOf(targetId).equals(e.getTargetId()))
        .toList();
  }
}
