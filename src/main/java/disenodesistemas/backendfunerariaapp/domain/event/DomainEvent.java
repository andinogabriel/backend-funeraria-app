package disenodesistemas.backendfunerariaapp.domain.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker for every domain event published through the transactional outbox (ADR-0013).
 *
 * <p>Sealed so the relay (and future consumers that deserialize the JSON payload back into
 * Java types) can use exhaustive {@code switch} expressions and so ArchUnit can enforce that
 * no event class is added outside this package without an explicit decision. Each
 * implementation is a record carrying the minimum payload a downstream consumer needs to
 * route or summarise the event; richer reconstructions are the consumer's job.
 *
 * <h3>JSON polymorphism</h3>
 *
 * The {@code @type} discriminator is written by Jackson into the outbox row's {@code payload}
 * column so when a consumer deserializes the JSON back into Java the right record subclass
 * is picked. The discriminator value is the simple class name, which is stable as long as we
 * do not rename event classes — adding new events is safe, renaming old ones is a contract
 * change that needs a consumer-side migration.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
public sealed interface DomainEvent
    permits FuneralCreated,
        FuneralUpdated,
        FuneralDeleted,
        AffiliateCreated,
        AffiliateUpdated,
        AffiliateMarkedDeceased,
        AffiliateDeleted {

  /** Aggregate type the event applies to ({@code FUNERAL}, {@code AFFILIATE}). */
  String aggregateType();

  /** Stable identifier of the target aggregate as a string (covers Long ids and dni Integers). */
  String aggregateId();

  /** Catalog entry classifying the event for routing and reporting. */
  String eventType();
}
