package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AuditEventRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed {@link AuditEventPort} implementation. Every recorded event is persisted in its own
 * REQUIRES_NEW transaction so an audit failure cannot mask a successful business write, and a
 * business rollback after the audit insert does not erase the trail. The read side uses a
 * read-only transaction so the search query never escalates locks against the append-only table.
 *
 * <p>The adapter resolves the trace and correlation identifiers from {@link
 * RequestTraceContext} (which reads the SLF4J MDC populated by Spring tracing and the
 * application's {@code RequestTracingFilter}). Callers therefore only provide business fields;
 * cross-cutting metadata is wired here.
 */
@Component
@Slf4j
public class JpaAuditEventPersistenceAdapter implements AuditEventPort {

  private final AuditEventRepository auditEventRepository;
  private final Clock clock;

  /**
   * Production-time constructor wired by Spring. Defaults the timestamp source to
   * {@link Clock#systemUTC()} so the adapter behaves correctly without an extra {@code @Bean}
   * declaration; tests use the overload below to inject a deterministic clock.
   */
  @Autowired
  public JpaAuditEventPersistenceAdapter(final AuditEventRepository auditEventRepository) {
    this(auditEventRepository, Clock.systemUTC());
  }

  /**
   * Test-friendly constructor that lets a fixed or stepped {@link Clock} drive the
   * {@code occurred_at} stamp so assertions can compare against an exact instant.
   */
  public JpaAuditEventPersistenceAdapter(
      final AuditEventRepository auditEventRepository, final Clock clock) {
    this.auditEventRepository = auditEventRepository;
    this.clock = clock;
  }

  /**
   * Builds the entity from the supplied business fields plus the active trace context, persists
   * it in an independent transaction and emits a structured log line so the same event is also
   * visible in the log pipeline. The {@code REQUIRES_NEW} propagation guarantees independence
   * from the caller's transaction state.
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(
      final AuditAction action,
      final String actorEmail,
      final Long actorId,
      final String targetType,
      final String targetId,
      final String payload) {
    final Instant occurredAt = Instant.now(clock);
    final String traceId = RequestTraceContext.currentTraceId();
    final String correlationId = RequestTraceContext.currentCorrelationId();

    final AuditEvent event =
        new AuditEvent(
            occurredAt,
            actorEmail,
            actorId,
            action,
            targetType,
            targetId,
            traceId,
            correlationId,
            payload);
    auditEventRepository.save(event);

    log.atInfo()
        .addKeyValue("event", "audit.event.recorded")
        .addKeyValue("action", action.name())
        .addKeyValue("actorEmail", actorEmail)
        .addKeyValue("targetType", targetType)
        .addKeyValue("targetId", targetId)
        .log("audit.event.recorded");
  }

  /**
   * Delegates the filtered search to the repository. Sorting is fixed inside the JPQL query
   * (most recent first) so the caller's {@link Pageable} only controls slicing.
   */
  @Override
  @Transactional(readOnly = true)
  public Page<AuditEvent> search(
      final String actorEmail,
      final AuditAction action,
      final String targetType,
      final String targetId,
      final Instant from,
      final Instant to,
      final Pageable pageable) {
    return auditEventRepository.search(
        actorEmail, action, targetType, targetId, from, to, pageable);
  }
}
