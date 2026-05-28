package disenodesistemas.backendfunerariaapp.domain.event;

/**
 * Emitted after an income (compra) is annulled. Carries both the original receipt id
 * and the freshly-minted reversal id so downstream consumers (low-stock notifications
 * in PR5b, analytical sinks) can reconstruct the pair without joining back to the
 * income table.
 */
public record IncomeAnnulled(Long originalId, Long reversalId) implements DomainEvent {

  @Override
  public String aggregateType() {
    return "INCOME";
  }

  @Override
  public String aggregateId() {
    return String.valueOf(originalId);
  }

  @Override
  public String eventType() {
    return "INCOME_ANNULLED";
  }
}
