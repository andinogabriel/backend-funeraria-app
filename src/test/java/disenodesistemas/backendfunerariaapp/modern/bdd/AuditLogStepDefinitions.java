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
   * Test-owned user emails the {@link Before} hook resets between scenarios. Listing them
   * explicitly keeps the cleanup scope narrow (no risk of wiping seeded users from V2) and
   * makes adding a new BDD-only persona an obvious one-line change.
   */
  private static final List<String> BDD_USER_EMAILS =
      List.of("admin@bdd.local", "auditee@bdd.local");

  /**
   * Resets the audit log, strips the role set off every BDD-owned user, and clears the
   * Spring Security context so scenarios start from a known state. Cucumber-Spring shares
   * one Spring context across the run, so role assignments persist between scenarios
   * unless we explicitly wipe them — without that reset, a second scenario granting the
   * same role would always short-circuit on idempotency and produce zero audit events.
   */
  @Before
  public void resetAuditState() {
    auditEventRepository.deleteAll();
    BDD_USER_EMAILS.forEach(
        email ->
            userPersistencePort
                .findByEmail(email)
                .ifPresent(
                    user -> {
                      user.getRoles().clear();
                      userPersistencePort.save(user);
                    }));
    SecurityContextHolder.clearContext();
  }

  @Given("an admin {string} is authenticated")
  public void anAdminIsAuthenticated(final String email) {
    final UserEntity admin = ensureUserWithRole(email, "BDD", "Admin", ADMIN_PASSWORD_HASH, Role.ROLE_ADMIN);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new TestingAuthenticationToken(admin.getEmail(), null, "ROLE_ADMIN"));
  }

  /**
   * Ensures a non-admin user exists, deliberately without any role attached. The role
   * grants exercised by the {@code When} steps must be observable as fresh additions,
   * which would not be the case if the background pre-attached {@code ROLE_USER}: a
   * subsequent grant of the same role would hit the idempotent skip in
   * {@link UserRoleUseCase} and emit nothing.
   */
  @Given("a regular user {string} exists in the system")
  public void aRegularUserExists(final String email) {
    ensureUser(email, "BDD", "Auditee", AUDITEE_PASSWORD_HASH);
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
   * Creates the user when missing, with no roles attached. Used by {@code aRegularUserExists}
   * so the role-grant {@code When} steps can be measured as net-new additions instead of
   * being short-circuited by the idempotent skip when the role is already there.
   */
  private UserEntity ensureUser(
      final String email,
      final String firstName,
      final String lastName,
      final String passwordHash) {
    return userPersistencePort
        .findByEmail(email)
        .orElseGet(
            () -> {
              final UserEntity created =
                  new UserEntity(email, firstName, lastName, passwordHash);
              created.activate();
              created.setEnabled(true);
              return userPersistencePort.save(created);
            });
  }

  /**
   * Creates the user when missing and ensures the supplied role is attached. Idempotent
   * across scenarios so the admin background can re-run cleanly after the {@code @Before}
   * reset stripped the role set.
   */
  private UserEntity ensureUserWithRole(
      final String email,
      final String firstName,
      final String lastName,
      final String passwordHash,
      final Role roleName) {
    final UserEntity user = ensureUser(email, firstName, lastName, passwordHash);
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
