package disenodesistemas.backendfunerariaapp.domain.event;

import java.math.BigDecimal;

/**
 * Emitted after a funeral service is successfully registered. Carries enough payload for a
 * downstream consumer to build a receipt summary, notify the funeral director or sync to a
 * reporting pipeline without joining back to the funeral table.
 */
public record FuneralCreated(
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
    return "FUNERAL_CREATED";
  }
}
