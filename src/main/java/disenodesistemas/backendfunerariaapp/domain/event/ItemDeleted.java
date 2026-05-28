package disenodesistemas.backendfunerariaapp.domain.event;

/**
 * Emitted after an item is soft-deleted. Carries the id + the natural-key code so
 * downstream consumers (notifications, analytical sinks) can summarise the event
 * without joining back to the items table — the row is still alive in DB but
 * filtered out of every operational read.
 */
public record ItemDeleted(Long itemId, String code) implements DomainEvent {

  @Override
  public String aggregateType() {
    return "ITEM";
  }

  @Override
  public String aggregateId() {
    return String.valueOf(itemId);
  }

  @Override
  public String eventType() {
    return "ITEM_DELETED";
  }
}
