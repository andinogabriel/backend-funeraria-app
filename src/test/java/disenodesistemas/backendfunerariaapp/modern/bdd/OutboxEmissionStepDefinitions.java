package disenodesistemas.backendfunerariaapp.modern.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import io.cucumber.java.en.Then;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Outbox-side assertions used by {@code outbox_emission.feature}. Reads {@code outbox_events}
 * rows directly so the steps verify the persisted contract between
 * {@link disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase}
 * and the outbox port — without involving the relay (the schedule runs on a 10-second initial
 * delay, so a scenario completing in milliseconds is guaranteed to see rows in PENDING).
 *
 * <p>State cleanup is the responsibility of {@link AffiliateLifecycleStepDefinitions} — both
 * features run inside the same Cucumber-Spring context and that class' {@code @Before} hook
 * wipes the outbox table between scenarios. Centralising cleanup avoids the ordering trap
 * where two {@code @Before} hooks compete to truncate the same table.
 */
public class OutboxEmissionStepDefinitions {

  private final OutboxEventRepository outboxEventRepository;

  @Autowired
  public OutboxEmissionStepDefinitions(final OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Then(
      "exactly {int} outbox event of type {string} is pending for aggregate {string} id {string}")
  public void exactlyOneOutboxEventIsPending(
      final int expectedCount,
      final String eventType,
      final String aggregateType,
      final String aggregateId) {
    final List<OutboxEvent> matching = matching(eventType, aggregateType, aggregateId);
    assertThat(matching)
        .as("%s outbox events for %s id %s", eventType, aggregateType, aggregateId)
        .hasSize(expectedCount);
    matching.forEach(row -> assertThat(row.getStatus()).isEqualTo(OutboxStatus.PENDING));
  }

  @Then("no outbox event of type {string} is pending for aggregate {string} id {string}")
  public void noOutboxEventIsPending(
      final String eventType, final String aggregateType, final String aggregateId) {
    assertThat(matching(eventType, aggregateType, aggregateId)).isEmpty();
  }

  @Then(
      "the AffiliateCreated outbox payload for aggregate id {string} carries the affiliate's first name {string}")
  public void theAffiliateCreatedPayloadCarriesFirstName(
      final String aggregateId, final String expectedFirstName) {
    final List<OutboxEvent> matching = matching("AFFILIATE_CREATED", "AFFILIATE", aggregateId);
    assertThat(matching).isNotEmpty();
    // Light-weight payload check: the JSON serialisation puts the first name as `"firstName":"..."`,
    // and verifying a substring is enough to confirm the event record was populated from the
    // saved entity. Full JSON deserialisation lives in OutboxAdapterPostgresIntegrationTest.
    assertThat(matching.getLast().getPayload())
        .as("AffiliateCreated payload for dni %s", aggregateId)
        .contains("\"firstName\":\"" + expectedFirstName + "\"");
  }

  private List<OutboxEvent> matching(
      final String eventType, final String aggregateType, final String aggregateId) {
    return outboxEventRepository.findAll().stream()
        .filter(e -> eventType.equals(e.getEventType()))
        .filter(e -> aggregateType.equals(e.getAggregateType()))
        .filter(e -> aggregateId.equals(e.getAggregateId()))
        .toList();
  }
}
