package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AuditEventRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * End-to-end smoke for the audit log foundation: writes through the port land in PostgreSQL with
 * the expected columns, and the active SLF4J MDC trace/correlation identifiers are captured by
 * the adapter without callers having to pass them.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuditEventPersistencePostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private AuditEventPort auditEventPort;
  @Autowired private AuditEventRepository auditEventRepository;

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  @DisplayName(
      "Given a recorded audit event when the row is queried back then the business fields are persisted as supplied")
  void shouldPersistAuditEventWithBusinessFields() {
    auditEventRepository.deleteAll();

    auditEventPort.record(
        AuditAction.AFFILIATE_CREATED,
        "admin@example.com",
        42L,
        "AFFILIATE",
        "12345678",
        "{\"firstName\":\"John\"}");

    final List<AuditEvent> all = auditEventRepository.findAll();
    assertThat(all).hasSize(1);
    final AuditEvent saved = all.getFirst();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getAction()).isEqualTo(AuditAction.AFFILIATE_CREATED);
    assertThat(saved.getActorEmail()).isEqualTo("admin@example.com");
    assertThat(saved.getActorId()).isEqualTo(42L);
    assertThat(saved.getTargetType()).isEqualTo("AFFILIATE");
    assertThat(saved.getTargetId()).isEqualTo("12345678");
    assertThat(saved.getPayload()).isEqualTo("{\"firstName\":\"John\"}");
    assertThat(saved.getOccurredAt()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
  }

  @Test
  @DisplayName(
      "Given trace and correlation identifiers in the SLF4J MDC when an audit event is recorded then both are persisted alongside the business fields")
  void shouldCaptureTraceAndCorrelationIdsFromMdc() {
    auditEventRepository.deleteAll();
    MDC.put(RequestTraceContext.TRACE_ID_MDC_KEY, "4bf92f3577b34da6a3ce929d0e0e4736");
    MDC.put(RequestTraceContext.CORRELATION_ID_MDC_KEY, "corr-abc-001");

    auditEventPort.record(
        AuditAction.USER_ROLE_GRANTED,
        "admin@example.com",
        42L,
        "USER",
        "user@example.com",
        null);

    final AuditEvent saved = auditEventRepository.findAll().getFirst();
    assertThat(saved.getTraceId()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
    assertThat(saved.getCorrelationId()).isEqualTo("corr-abc-001");
    assertThat(saved.getPayload()).isNull();
  }

  @Test
  @DisplayName(
      "Given no MDC entries when an audit event is recorded then the row is persisted with null trace and correlation identifiers")
  void shouldPersistWithoutMdcWhenNoTraceContextIsActive() {
    auditEventRepository.deleteAll();

    auditEventPort.record(
        AuditAction.FUNERAL_DELETED,
        "system@example.com",
        null,
        "FUNERAL",
        "9001",
        null);

    final AuditEvent saved = auditEventRepository.findAll().getFirst();
    assertThat(saved.getActorId()).isNull();
    assertThat(saved.getTraceId()).isNull();
    assertThat(saved.getCorrelationId()).isNull();
  }
}
