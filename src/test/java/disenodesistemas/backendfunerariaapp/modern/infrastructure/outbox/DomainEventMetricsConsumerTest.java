package disenodesistemas.backendfunerariaapp.modern.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralCreated;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.DomainEventMetricsConsumer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainEventMetricsConsumerTest {

  @Test
  @DisplayName(
      "Given domain events when consumed then a counter per event and aggregate type is incremented")
  void countsPerEventType() {
    final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    final DomainEventMetricsConsumer consumer = new DomainEventMetricsConsumer(registry);
    final DomainEvent event =
        new FuneralCreated(1L, "REC-X", "S", BigDecimal.ZERO, 1L, 30111222, "Juan Perez");

    consumer.consume(event, null);
    consumer.consume(event, null);

    assertThat(
            registry
                .get("domain.events")
                .tag("eventType", event.eventType())
                .tag("aggregateType", event.aggregateType())
                .counter()
                .count())
        .isEqualTo(2);
    assertThat(consumer.name()).isEqualTo("domain-event-metrics");
  }
}
