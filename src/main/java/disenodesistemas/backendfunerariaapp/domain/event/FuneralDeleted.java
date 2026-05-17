package disenodesistemas.backendfunerariaapp.domain.event;

/**
 * Emitted after a funeral is hard-deleted. Carries only the id because the row is gone — any
 * consumer that needed the prior state should have materialised it from {@link FuneralCreated}
 * / {@link FuneralUpdated} events.
 */
public record FuneralDeleted(Long funeralId) implements DomainEvent {

  @Override
  public String aggregateType() {
    return "FUNERAL";
  }

  @Override
  public String aggregateId() {
    return String.valueOf(funeralId);
  }

  @Override
  public String eventType() {
    return "FUNERAL_DELETED";
  }
}
