package disenodesistemas.backendfunerariaapp.domain.event;

import java.math.BigDecimal;

/**
 * Emitted after a funeral is updated. Same payload shape as {@link FuneralCreated} so
 * consumers can treat creates and updates uniformly (latest-write-wins materialised view
 * pattern) or branch on the event type when they care about the distinction.
 */
public record FuneralUpdated(
    Long funeralId,
    String receiptNumber,
    String receiptSeries,
    BigDecimal totalAmount,
    Long planId,
    Integer deceasedDni,
    String deceasedFullName)
    implements DomainEvent {

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
    return "FUNERAL_UPDATED";
  }
}
