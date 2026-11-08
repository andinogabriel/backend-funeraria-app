package disenodesistemas.backendfunerariaapp.modern.infrastructure.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.application.port.out.IdempotencyStorePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.domain.entity.IdempotencyRecord;
import disenodesistemas.backendfunerariaapp.infrastructure.idempotency.IdempotencyFilter;
import disenodesistemas.backendfunerariaapp.infrastructure.idempotency.IdempotencyProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class IdempotencyFilterTest {

  private static final Instant NOW = Instant.parse("2026-11-08T12:00:00Z");
  private static final String KEY = "0f8fad5b-d9cb-469f-a165-70867728950e";
  private static final String BODY = "{\"name\":\"Cofre\"}";

  private final IdempotencyStorePort store = mock(IdempotencyStorePort.class);
  private final MessageResolverPort messages = mock(MessageResolverPort.class);
  private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
  private IdempotencyFilter filter;

  /** Counts how many times the downstream chain (the business operation) actually ran. */
  private final AtomicInteger executions = new AtomicInteger();

  @BeforeEach
  void setUp() {
    filter =
        new IdempotencyFilter(
            store,
            messages,
            new ObjectMapper(),
            registry,
            new SecurityRequestProperties("X-Device-Id", "Idempotency-Key", "secret", true),
            new IdempotencyProperties(24, 1_048_576, 3_600_000),
            Clock.fixed(NOW, ZoneOffset.UTC));
    when(messages.getMessage(anyString())).thenReturn("msg");
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin@funeraria.local", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  private MockHttpServletRequest post(final String body) {
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/items");
    request.addHeader("Idempotency-Key", KEY);
    request.setContentType("application/json");
    request.setContent(body.getBytes(StandardCharsets.UTF_8));
    return request;
  }

  private FilterChain chainReturning(final int status, final String responseBody) {
    return (req, res) -> {
      executions.incrementAndGet();
      // The downstream handler must still be able to read the body the filter consumed.
      assertThat(new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo(BODY);
      ((HttpServletResponse) res).setStatus(status);
      res.setContentType("application/json");
      res.getWriter().write(responseBody);
    };
  }

  private IdempotencyRecord completedRecord(final String hash) {
    final IdempotencyRecord record =
        IdempotencyRecord.reserve(
            "admin@funeraria.local", KEY, "POST", "/api/v1/items", hash, NOW, NOW.plusSeconds(3600));
    record.complete(201, "application/json", "{\"id\":7}");
    return record;
  }

  private static String hashOf(final MockHttpServletRequest request) {
    return (String)
        ReflectionTestUtils.invokeMethod(
            IdempotencyFilter.class,
            "requestHash",
            request,
            request.getContentAsByteArray());
  }

  @Test
  @DisplayName("Given a first request with a key when filtered then it runs once and its response is stored")
  void firstRequestRunsAndIsStored() throws Exception {
    when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
    when(store.tryReserve(any())).thenReturn(true);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(post(BODY), response, chainReturning(201, "{\"id\":7}"));

    assertThat(executions).hasValue(1);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getContentAsString()).isEqualTo("{\"id\":7}");
    verify(store).complete(any(), eq(201), eq("application/json"), eq("{\"id\":7}"));
  }

  @Test
  @DisplayName("Given a completed key and the same request when retried then the stored response is replayed without running again")
  void retryReplaysStoredResponse() throws Exception {
    final MockHttpServletRequest request = post(BODY);
    when(store.find("admin@funeraria.local", KEY))
        .thenReturn(Optional.of(completedRecord(hashOf(request))));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chainReturning(201, "{\"id\":8}"));

    assertThat(executions).hasValue(0);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getHeader("Idempotency-Replayed")).isEqualTo("true");
    assertThat(response.getContentAsString()).isEqualTo("{\"id\":7}");
    assertThat(registry.get("idempotency.requests").tag("outcome", "replayed").counter().count())
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Given a completed key reused with a different body when filtered then it is rejected with 422")
  void differentPayloadIsRejected() throws Exception {
    when(store.find("admin@funeraria.local", KEY))
        .thenReturn(Optional.of(completedRecord("another-request-hash")));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(post(BODY), response, chainReturning(201, "{}"));

    assertThat(executions).hasValue(0);
    assertThat(response.getStatus()).isEqualTo(422);
    assertThat(response.getContentAsString()).contains("idempotency.error.payload.mismatch");
  }

  @Test
  @DisplayName("Given the first request still running when retried then it answers 409 flagged in-progress")
  void inProgressAnswers409() throws Exception {
    final MockHttpServletRequest request = post(BODY);
    final IdempotencyRecord running =
        IdempotencyRecord.reserve(
            "admin@funeraria.local", KEY, "POST", "/api/v1/items", hashOf(request), NOW, NOW.plusSeconds(3600));
    when(store.find("admin@funeraria.local", KEY)).thenReturn(Optional.of(running));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chainReturning(201, "{}"));

    assertThat(executions).hasValue(0);
    assertThat(response.getStatus()).isEqualTo(409);
    assertThat(response.getHeader("Idempotency-Status")).isEqualTo("in-progress");
  }

  @Test
  @DisplayName("Given a 5xx outcome when filtered then the key is released so the client can retry")
  void serverErrorReleasesKey() throws Exception {
    when(store.find(anyString(), anyString())).thenReturn(Optional.empty());
    when(store.tryReserve(any())).thenReturn(true);

    filter.doFilter(post(BODY), new MockHttpServletResponse(), chainReturning(503, "{}"));

    verify(store).release(any());
    verify(store, never()).complete(any(), anyInt(), anyString(), any());
  }

  @Test
  @DisplayName("Given the reservation loses a race when filtered then it answers 409 instead of running twice")
  void lostReservationRaceAnswers409() throws Exception {
    final MockHttpServletRequest request = post(BODY);
    final IdempotencyRecord winner =
        IdempotencyRecord.reserve(
            "admin@funeraria.local", KEY, "POST", "/api/v1/items", hashOf(request), NOW, NOW.plusSeconds(3600));
    when(store.find("admin@funeraria.local", KEY))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(winner));
    when(store.tryReserve(any())).thenReturn(false);
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chainReturning(201, "{}"));

    assertThat(executions).hasValue(0);
    assertThat(response.getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("Given a malformed key when filtered then it is rejected with 400 and nothing runs")
  void malformedKeyIsRejected() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/items");
    request.addHeader("Idempotency-Key", "bad key!");
    request.setContent(BODY.getBytes(StandardCharsets.UTF_8));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chainReturning(201, "{}"));

    assertThat(executions).hasValue(0);
    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("Given an anonymous request when filtered then it passes through without touching the store")
  void anonymousPassesThrough() throws Exception {
    SecurityContextHolder.clearContext();

    filter.doFilter(post(BODY), new MockHttpServletResponse(), chainReturning(201, "{}"));

    assertThat(executions).hasValue(1);
    verify(store, never()).find(anyString(), anyString());
  }

  @Test
  @DisplayName("Given a GET or a request without key or an auth endpoint when filtered then idempotency is skipped")
  void skippedRequests() throws Exception {
    final MockHttpServletRequest get = new MockHttpServletRequest("GET", "/api/v1/items");
    get.addHeader("Idempotency-Key", KEY);
    final MockHttpServletRequest noKey = new MockHttpServletRequest("POST", "/api/v1/items");
    final MockHttpServletRequest login = new MockHttpServletRequest("POST", "/api/v1/users/login");
    login.addHeader("Idempotency-Key", KEY);

    for (final MockHttpServletRequest request : List.of(get, noKey, login)) {
      filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> executions.incrementAndGet());
    }

    assertThat(executions).hasValue(3);
    verify(store, never()).find(anyString(), anyString());
    verify(store, never()).tryReserve(isNull());
  }
}
