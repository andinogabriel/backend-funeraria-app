package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralCreated;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.OutboxRelay;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * End-to-end smoke for the outbox foundation (ADR-0013): a {@code publish} call writes a
 * {@code PENDING} row whose JSON payload carries the full event record + trace context, and
 * the relay drains the row to {@code PUBLISHED} in a separate transaction.
 *
 * <p>{@code publish} calls run inside an explicit {@link TransactionTemplate} block because
 * the adapter is configured with {@code propagation = MANDATORY} (a safety net against use
 * cases forgetting to wrap their call). In production every call site already sits inside a
 * use-case transaction; the test reproduces that envelope explicitly so we exercise the same
 * propagation contract.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OutboxAdapterPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private OutboxPort outboxPort;
  @Autowired private OutboxRelay outboxRelay;
  @Autowired private OutboxEventRepository repository;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TransactionTemplate transactionTemplate;

  @AfterEach
  void clearStateBetweenTests() {
    repository.deleteAll();
    MDC.clear();
  }

  @Test
  @DisplayName(
      "Given a published domain event when the row is queried then it is persisted as PENDING with the JSON payload + trace context")
  void shouldPersistPendingRowWithJsonPayloadAndTraceContext() throws Exception {
    MDC.put(RequestTraceContext.TRACE_ID_MDC_KEY, "trace-001");
    MDC.put(RequestTraceContext.CORRELATION_ID_MDC_KEY, "corr-001");

    final FuneralCreated event =
        new FuneralCreated(
            42L,
            "REC-001",
            "0001",
            new BigDecimal("250000"),
            7L,
            30111222,
            "Juan Perez");

    transactionTemplate.executeWithoutResult(status -> outboxPort.publish(event));

    final var saved = repository.findAll().getFirst();
    assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
    assertThat(saved.getEventType()).isEqualTo("FUNERAL_CREATED");
    assertThat(saved.getAggregateType()).isEqualTo("FUNERAL");
    assertThat(saved.getAggregateId()).isEqualTo("42");
    assertThat(saved.getEventId()).isNotNull();
    assertThat(saved.getOccurredAt()).isCloseTo(Instant.now(), within(1, ChronoUnit.MINUTES));
    assertThat(saved.getTraceId()).isEqualTo("trace-001");
    assertThat(saved.getCorrelationId()).isEqualTo("corr-001");
    assertThat(saved.getAttempts()).isZero();

    // Round-trip the JSON payload back to a FuneralCreated record to lock the Jackson
    // polymorphism setup: Java type → JSON (with @type discriminator) → Java type.
    final FuneralCreated decoded =
        objectMapper.readValue(saved.getPayload(), FuneralCreated.class);
    assertThat(decoded).isEqualTo(event);
  }

  @Test
  @DisplayName(
      "Given a pending outbox row when the relay drains then the row is flipped to PUBLISHED with a publishedAt stamp")
  void relayShouldDrainPendingRowsToPublished() {
    publishSampleEvent();

    outboxRelay.drain();

    final OutboxEvent saved = repository.findAll().getFirst();
    assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(saved.getPublishedAt()).isNotNull();
    assertThat(saved.getAttempts()).isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given multiple pending rows when the relay drains then every row is flipped to PUBLISHED")
  void relayShouldDrainAllPendingRowsInOneTick() {
    publishSampleEvent();
    publishSampleEvent();
    publishSampleEvent();

    outboxRelay.drain();

    assertThat(repository.countByStatus(OutboxStatus.PUBLISHED)).isEqualTo(3);
    assertThat(repository.countByStatus(OutboxStatus.PENDING)).isZero();
  }

  private void publishSampleEvent() {
    transactionTemplate.executeWithoutResult(
        status ->
            outboxPort.publish(
                new FuneralCreated(
                    1L, "REC-X", "S", BigDecimal.ZERO, 1L, 30111222, "Juan Perez")));
  }
}
