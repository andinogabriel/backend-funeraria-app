package disenodesistemas.backendfunerariaapp.domain.event;

/**
 * Emitted after a plan is soft-deleted. Carries only the id — consumers needing the
 * prior state can rebuild it from the create event stream (when one ships) or query
 * the row directly through the admin papelera surface, since soft-delete keeps the
 * record alive.
 */
public record PlanDeleted(Long planId) implements DomainEvent {

  @Override
  public String aggregateType() {
    return "PLAN";
  }

  @Override
  public String aggregateId() {
    return String.valueOf(planId);
  }

  @Override
  public String eventType() {
    return "PLAN_DELETED";
  }
}
