package disenodesistemas.backendfunerariaapp.domain.event;

/**
 * Emitted after an affiliate is hard-deleted. Carries only the dni; consumers needing the
 * prior state should have materialised it from {@link AffiliateCreated} / {@link AffiliateUpdated}.
 */
public record AffiliateDeleted(Integer dni) implements DomainEvent {

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
    return "AFFILIATE_DELETED";
  }
}
