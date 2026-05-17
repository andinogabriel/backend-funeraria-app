package disenodesistemas.backendfunerariaapp.domain.event;

import java.time.LocalDate;

/**
 * Emitted after a new affiliate is registered. Carries identity + the relationship the
 * affiliate has with the policy holder so a downstream CRM sync can build the customer
 * record without an additional read.
 */
public record AffiliateCreated(
    Integer dni,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String genderName,
    String relationshipName,
    String policyHolderEmail)
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
    return "AFFILIATE_CREATED";
  }
}
