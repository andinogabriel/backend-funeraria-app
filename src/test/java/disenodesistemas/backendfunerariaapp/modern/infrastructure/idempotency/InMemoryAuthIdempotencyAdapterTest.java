package disenodesistemas.backendfunerariaapp.modern.infrastructure.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.infrastructure.idempotency.InMemoryAuthIdempotencyAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.AuthIdempotencyProperties;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InMemoryAuthIdempotencyAdapter")
class InMemoryAuthIdempotencyAdapterTest {

  private final InMemoryAuthIdempotencyAdapter adapter =
      new InMemoryAuthIdempotencyAdapter(new AuthIdempotencyProperties(120));

  @Test
  @DisplayName(
      "Given a previously stored login response when the same idempotency key and payload fingerprint are reused then it returns the cached JWT response")
  void givenAPreviouslyStoredLoginResponseWhenTheSameIdempotencyKeyAndPayloadFingerprintAreReusedThenItReturnsTheCachedJwtResponse() {
    final JwtDto response =
        JwtDto.builder()
            .authorization("Bearer access-token")
            .refreshToken("refresh-token")
            .expiryDuration(900_000L)
            .authorities(List.of("ROLE_USER"))
            .build();

    adapter.storeJwtResponse("user.login", "idem-1", "fp-1", response);

    assertThat(adapter.findJwtResponse("user.login", "idem-1", "fp-1")).contains(response);
  }

  @Test
  @DisplayName(
      "Given a stored idempotency key when the same key is reused with a different payload fingerprint then it rejects the replay as a conflict")
  void givenAStoredIdempotencyKeyWhenTheSameKeyIsReusedWithADifferentPayloadFingerprintThenItRejectsTheReplayAsAConflict() {
    adapter.storeJwtResponse(
        "user.refresh",
        "idem-1",
        "fp-1",
        JwtDto.builder().authorization("Bearer access-token").refreshToken("refresh-token").build());

    assertThatThrownBy(() -> adapter.findJwtResponse("user.refresh", "idem-1", "fp-2"))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("idempotency.error.payload.mismatch");
  }

  @Test
  @DisplayName(
      "Given a blank idempotency key when a response is looked up or stored then the adapter behaves as a no-op")
  void givenABlankIdempotencyKeyWhenAResponseIsLookedUpOrStoredThenTheAdapterBehavesAsANoOp() {
    final JwtDto response =
        JwtDto.builder()
            .authorization("Bearer access-token")
            .refreshToken("refresh-token")
            .expiryDuration(900_000L)
            .authorities(List.of("ROLE_USER"))
            .build();

    adapter.storeJwtResponse("user.login", "   ", "fp-1", response);

    assertThat(adapter.findJwtResponse("user.login", "   ", "fp-1")).isEmpty();
  }

  @Test
  @DisplayName(
      "Given an unused idempotency key when a cached response is requested then the adapter returns empty")
  void givenAnUnusedIdempotencyKeyWhenACachedResponseIsRequestedThenTheAdapterReturnsEmpty() {
    assertThat(adapter.findJwtResponse("user.login", "idem-missing", "fp-1")).isEmpty();
  }
}
