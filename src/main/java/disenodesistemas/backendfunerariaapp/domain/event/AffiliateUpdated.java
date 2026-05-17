package disenodesistemas.backendfunerariaapp.domain.event;

import java.time.LocalDate;

/**
 * Emitted after an affiliate's mutable fields change. Same payload shape as
 * {@link AffiliateCreated} so a downstream materialised view can latest-write-wins without
 * branching. When the update also flips the {@code deceased} flag from {@code false} to
 * {@code true}, {@link AffiliateMarkedDeceased} is emitted alongside this event so consumers
 * that only care about the lifecycle transition can subscribe without diffing snapshots.
 */
public record AffiliateUpdated(
    Integer dni,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String genderName,
    String relationshipName,
    boolean deceased)
    implements DomainEvent {

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
    return "AFFILIATE_UPDATED";
  }
}
