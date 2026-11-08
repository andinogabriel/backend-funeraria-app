package disenodesistemas.backendfunerariaapp.modern.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.port.out.IdempotencyStorePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IdempotencyRecord;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Exercises the idempotency store against real PostgreSQL: the unique (principal, key)
 * constraint is the concurrency guard, so it has to be tested on the real database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class IdempotencyStorePostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  private static final Instant NOW = Instant.parse("2026-11-08T12:00:00Z");

  @Autowired private IdempotencyStorePort store;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clean() {
    jdbcTemplate.update("delete from idempotency_keys");
  }

  private static IdempotencyRecord reservation(final String principal, final String key, final Instant expiresAt) {
    return IdempotencyRecord.reserve(principal, key, "POST", "/api/v1/items", "a".repeat(64), NOW, expiresAt);
  }

  @Test
  @DisplayName("Given a reserved key when the same principal reserves it again then the second reservation returns false without throwing")
  void duplicateReservationReturnsFalse() {
    assertThat(store.tryReserve(reservation("ana@x.com", "key-00000001", NOW.plusSeconds(3600)))).isTrue();
    assertThat(store.tryReserve(reservation("ana@x.com", "key-00000001", NOW.plusSeconds(3600)))).isFalse();

    // The store is still usable after the rejected insert (no poisoned transaction).
    assertThat(store.find("ana@x.com", "key-00000001")).isPresent();
  }

  @Test
  @DisplayName("Given the same key used by two principals when reserved then both succeed (keys are scoped per principal)")
  void keysAreScopedPerPrincipal() {
    assertThat(store.tryReserve(reservation("ana@x.com", "key-00000002", NOW.plusSeconds(3600)))).isTrue();
    assertThat(store.tryReserve(reservation("beto@x.com", "key-00000002", NOW.plusSeconds(3600)))).isTrue();
  }

  @Test
  @DisplayName("Given a reservation when completed then the stored response can be read back")
  void completeStoresResponse() {
    final IdempotencyRecord record = reservation("ana@x.com", "key-00000003", NOW.plusSeconds(3600));
    store.tryReserve(record);

    store.complete(record.getId(), 201, "application/json", "{\"id\":7}");

    final IdempotencyRecord stored = store.find("ana@x.com", "key-00000003").orElseThrow();
    assertThat(stored.getStatus()).isEqualTo(IdempotencyRecord.Status.COMPLETED);
    assertThat(stored.getResponseStatus()).isEqualTo(201);
    assertThat(stored.getResponseBody()).isEqualTo("{\"id\":7}");
  }

  @Test
  @DisplayName("Given a released reservation when the client retries then the key can be reserved again")
  void releaseAllowsRetry() {
    final IdempotencyRecord record = reservation("ana@x.com", "key-00000004", NOW.plusSeconds(3600));
    store.tryReserve(record);
    store.release(record.getId());

    assertThat(store.tryReserve(reservation("ana@x.com", "key-00000004", NOW.plusSeconds(3600)))).isTrue();
  }

  @Test
  @DisplayName("Given expired and live keys when purging then only the expired ones are deleted")
  void purgeDeletesOnlyExpired() {
    store.tryReserve(reservation("ana@x.com", "key-expired1", NOW.minusSeconds(1)));
    store.tryReserve(reservation("ana@x.com", "key-live0001", NOW.plusSeconds(3600)));

    assertThat(store.purgeExpired(NOW)).isEqualTo(1);
    assertThat(store.find("ana@x.com", "key-live0001")).isPresent();
    assertThat(store.find("ana@x.com", "key-expired1")).isEmpty();
  }
}
