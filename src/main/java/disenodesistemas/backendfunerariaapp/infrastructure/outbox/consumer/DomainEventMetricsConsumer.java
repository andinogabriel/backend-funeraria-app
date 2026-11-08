package disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer;

import disenodesistemas.backendfunerariaapp.application.port.out.DomainEventConsumer;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Turns the domain-event stream into business counters
 * ({@code domain_events_total{eventType,aggregateType}}): services registered, affiliates
 * created, incomes annulled, low-stock reached, ... — without instrumenting each use case.
 * Registered as one more outbox consumer, so the relay fans every event out to it.
 *
 * <p>The relay is at-least-once: a redelivered event is counted twice. That skew is negligible
 * for rate dashboards and alerts; authoritative totals still come from the database.
 */
@Component
public class DomainEventMetricsConsumer implements DomainEventConsumer {

  private static final String CONSUMER_NAME = "domain-event-metrics";

  private final MeterRegistry meterRegistry;

  public DomainEventMetricsConsumer(final MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public String name() {
    return CONSUMER_NAME;
  }

  @Override
  public void consume(final DomainEvent event, final EventEnvelope envelope) {
    Counter.builder("domain.events")
        .description("Domain events processed by the outbox relay")
        .tag("eventType", event.eventType())
        .tag("aggregateType", event.aggregateType())
        .register(meterRegistry)
        .increment();
  }
}
