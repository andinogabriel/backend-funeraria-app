package disenodesistemas.backendfunerariaapp.domain.event;

import java.time.Instant;

/**
 * Emitted when an affiliate update flips the {@code deceased} flag from {@code false} to
 * {@code true}. Companion to {@link AffiliateUpdated} for consumers that only care about the
 * lifecycle transition — e.g. an external CRM that needs to close the customer record, or a
 * reporting pipeline that tracks mortality counts.
 */
public record AffiliateMarkedDeceased(Integer dni, Instant markedAt) implements DomainEvent {

  @Override
  public String aggregateType() {
    return "AFFILIATE";
  }

  @Override
  public String aggregateId() {
    return String.valueOf(dni);
  }

  @Override
  public String eventType() {
    return "AFFILIATE_MARKED_DECEASED";
  }
}
