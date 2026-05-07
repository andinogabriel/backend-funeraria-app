# 0012. Cucumber BDD Bootstrap

## Status

Accepted

## Context

The test suite currently has three layers, each with a clear job:

- **Unit tests** with Mockito and `STRICT_STUBS` for application use cases — fast feedback,
  isolated assertions on a single class' behavior.
- **Postgres integration tests** under `modern.infrastructure.persistence` — Testcontainers
  + real Flyway migrations, exercising the JPA adapters with the schema we ship.
- **Architecture tests** with ArchUnit — package-boundary guardrails that fail fast when a
  refactor leaks across layers.

What is missing is a **scenario-shaped** layer: tests that read like the contract the
application offers (\"granting a role emits an audit event with the role name in the
payload\") rather than like the implementation (\"`UserRoleUseCase.updateUserRol` calls
`AuditEventPort.record` with a JSON-shaped string\"). For compliance-sensitive features
like the audit log, that framing matters: when a reviewer asks \"what guarantees does the
audit trail give us?\", a Gherkin scenario answers the question directly, and the same
file doubles as the regression test that proves the answer is still true.

The candidate tools were:

- **Cucumber-JVM with `cucumber-spring`** — first-class Spring Boot integration, JUnit
  Platform engine that Surefire already runs, mature ecosystem, English- and
  Spanish-keyword support if a non-developer audience ever needs to read the features.
- **Spock with data tables** — terse, but Groovy adds a second toolchain to a Maven build
  that is otherwise pure Java 25.
- **Karate** — opinionated toward HTTP tests; would force every scenario to go through the
  HTTP layer even when the audit emitter is what we actually care about.
- **Plain JUnit with `@DisplayName`** — already used; the readable names help, but they
  remain method-shaped and lose the Given/When/Then framing that makes scenarios useful for
  compliance review.

## Decision

Adopt Cucumber-JVM as a fourth, narrowly-scoped layer of the suite. Wire it through the
JUnit Platform so it runs inside the existing `mvn verify` lifecycle without a separate
profile or runner.

- **Dependencies** managed through the official Cucumber BOM
  (`io.cucumber:cucumber-bom:7.20.1`) so every `cucumber-*` artifact stays version-aligned.
  Added to the test scope only:
  - `cucumber-java` — annotation-based step definitions.
  - `cucumber-spring` — boots a Spring context per scenario.
  - `cucumber-junit-platform-engine` — registers Cucumber as a JUnit 5 engine.
  - `junit-platform-suite` — provides the `@Suite` runner Surefire discovers.
- **Discovery** lives in `src/test/resources/junit-platform.properties`. Features go
  under `src/test/resources/features/`; glue (step definitions and the
  `@CucumberContextConfiguration` class) stays under
  `disenodesistemas.backendfunerariaapp.modern.bdd`.
- **Build gating**: the Cucumber suite is excluded from the default Surefire pattern in
  `pom.xml` and is re-enabled by a Maven profile, `bdd`. CI invokes
  `mvn -Pbdd verify`; the default `mvn verify` keeps a Docker-less local build green.
  This is needed because Cucumber's JUnit Platform engine bootstraps the Spring context
  outside the JUnit Jupiter lifecycle that honors
  `@Testcontainers(disabledWithoutDocker = true)`, so simply annotating the suite class
  does not produce the same "skip without Docker" behavior the rest of the integration
  tests rely on. Gating at the build level is the cleanest workaround until Cucumber's
  engine integrates with Testcontainers' conditional execution natively.
- **Spring context** is anchored on a single class, `CucumberSpringConfiguration`, that
  inherits from `AbstractPostgresIntegrationTest` so BDD scenarios reuse the same
  Testcontainers PostgreSQL fixture as the rest of the integration suite. Cucumber
  rejects more than one `@CucumberContextConfiguration` on the classpath; the new
  `BddBoundaryGuardrailsTest` ArchUnit rule keeps every Cucumber import under `..bdd..`
  so the constraint is enforced statically.
- **Initial scope** is one feature, `audit_role_grant.feature`, with two scenarios that
  cover the most compliance-relevant guarantee the audit log offers (role grants are
  recorded, and idempotent grants do not duplicate the trail). The feature exercises the
  use case directly and asserts against `AuditEventRepository`, so it covers the
  application + persistence path end-to-end without the HTTP layer's overhead.
- **Feature language**: English. Matches the existing rule \"English in code, commits and
  PRs\" recorded in the project memory; Cucumber supports localized keywords, so a future
  reviewer audience can flip the dialect to Spanish per file without touching the
  step-definition Java.

## Consequences

**Pros**

- Compliance-sensitive flows get a documentation surface that doubles as a regression
  test. Dropping a `.feature` next to a new audit emitter is the cheapest way to declare
  what the trail guarantees.
- Cucumber runs in the same `mvn verify` invocation as JUnit and ArchUnit, so the gate
  is unified — no extra profile, no extra CI step, no extra command for contributors.
- The shared Spring context and the existing Postgres Testcontainers fixture keep the
  marginal cost of one new scenario at \"a few hundred milliseconds\" once the context is
  warm. We do not pay for a brand-new Spring boot per scenario.
- The ArchUnit rule guarantees the toolkit cannot creep into unrelated test packages.
  This is a real risk with Cucumber because step-definition classes look like ordinary
  Spring components and could accidentally be wired into the rest of the suite.

**Cons / trade-offs**

- A second testing style adds onboarding cost. We mitigate that by keeping the scope
  narrow: BDD is reserved for cross-cutting compliance/contract scenarios, not for the
  per-class behavior already covered by unit tests.
- Cucumber's reflection-heavy step lookup is harder to navigate from a stack trace than
  plain JUnit. Treating step definitions as a thin pass-through to use cases (no logic,
  no branching) keeps debugging painless when a scenario fails.
- The `@Before` hook clears the audit log between scenarios because the Spring context
  is shared. New step-definition classes must follow the same rule — assume the context
  is dirty, scope your reset narrowly. This is documented in the step-definition class
  to avoid drift.

## Validation

- `mvn verify` runs the full suite including the Cucumber feature; the new
  `BddBoundaryGuardrailsTest` ArchUnit rule is part of the same run.
- The two scenarios in `audit_role_grant.feature` cover (a) a single role grant emits
  one audit event with the role in the payload and (b) granting an already-assigned
  role does not duplicate the trail. Both run against PostgreSQL Testcontainers and
  exercise the application + persistence path end-to-end.

## References

- Cucumber-JVM Spring integration:
  https://cucumber.io/docs/cucumber/state/#sharing-state-using-spring
- Cucumber JUnit Platform Engine:
  https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-junit-platform-engine
- ADR-0010 — audit log read API (the surface the first feature exercises).
- ADR-0011 — performance baseline (sibling \"new test layer\" decision; same minimal
  bootstrap pattern).
