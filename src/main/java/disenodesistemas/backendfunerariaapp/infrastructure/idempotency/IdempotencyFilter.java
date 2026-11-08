package disenodesistemas.backendfunerariaapp.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.application.port.out.IdempotencyStorePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.domain.entity.IdempotencyRecord;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Server side of request idempotency: makes every mutating API call safe to retry when the
 * client sends an {@code Idempotency-Key} (the frontend's idempotency interceptor does it for all
 * POST / PUT / PATCH / DELETE requests).
 *
 * <ul>
 *   <li>First request with a key: reserved as IN_PROGRESS, executed, and its response stored.
 *   <li>Retry with the same key and the same request: the stored response is replayed
 *       ({@code Idempotency-Replayed: true}); the operation does not run again.
 *   <li>Same key while the first request still runs: 409 with
 *       {@code Idempotency-Status: in-progress} (the client retries with backoff).
 *   <li>Same key with a different method / path / body: 422, the key is being misused.
 *   <li>A 5xx outcome or an exception releases the key so the client may retry.
 * </ul>
 *
 * Runs inside the Spring Security chain after authorization, so keys are scoped to the
 * authenticated principal and rejected requests (401/403) never reserve one. Anonymous requests,
 * multipart uploads, oversized bodies and the auth endpoints (which have their own idempotency)
 * pass through untouched. Registered only in the security chain; its servlet-level registration
 * is disabled in {@link IdempotencyFilterConfig}, otherwise it would run before authentication.
 */
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

  static final String REPLAYED_HEADER = "Idempotency-Replayed";
  static final String STATUS_HEADER = "Idempotency-Status";

  private static final Set<String> MUTATING = Set.of("POST", "PUT", "PATCH", "DELETE");
  private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8,100}$");
  /** Endpoints with their own idempotency or where replay has no value. */
  private static final List<String> EXCLUDED_PREFIXES =
      List.of("/api/v1/users/login", "/api/v1/users/refresh", "/api/v1/client-telemetry");

  private final IdempotencyStorePort store;
  private final MessageResolverPort messages;
  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;
  private final String keyHeader;
  private final Duration ttl;
  private final long maxBodyBytes;
  private final Clock clock;

  public IdempotencyFilter(
      final IdempotencyStorePort store,
      final MessageResolverPort messages,
      final ObjectMapper objectMapper,
      final MeterRegistry meterRegistry,
      final SecurityRequestProperties securityRequestProperties,
      final IdempotencyProperties properties,
      final Clock clock) {
    this.store = store;
    this.messages = messages;
    this.objectMapper = objectMapper;
    this.meterRegistry = meterRegistry;
    this.keyHeader = securityRequestProperties.idempotencyKeyHeader();
    this.ttl = Duration.ofHours(properties.ttlHours());
    this.maxBodyBytes = properties.maxBodyBytes();
    this.clock = clock;
  }

  @Override
  protected boolean shouldNotFilter(final @NonNull HttpServletRequest request) {
    final String path = request.getRequestURI();
    return !MUTATING.contains(request.getMethod())
        || !Strings.CS.startsWith(path, "/api/")
        || EXCLUDED_PREFIXES.stream().anyMatch(prefix -> Strings.CS.startsWith(path, prefix))
        || StringUtils.isBlank(request.getHeader(keyHeader))
        || Strings.CI.startsWith(request.getContentType(), MediaType.MULTIPART_FORM_DATA_VALUE)
        || request.getContentLengthLong() > maxBodyBytes;
  }

  @Override
  protected void doFilterInternal(
      final @NonNull HttpServletRequest request,
      final @NonNull HttpServletResponse response,
      final @NonNull FilterChain chain)
      throws ServletException, IOException {
    final Optional<String> principal = currentPrincipal();
    if (principal.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }
    final String key = request.getHeader(keyHeader).trim();
    if (!KEY_PATTERN.matcher(key).matches()) {
      count("invalid_key");
      writeProblem(request, response, HttpStatus.BAD_REQUEST, "idempotency.error.key.invalid");
      return;
    }

    final byte[] body = request.getInputStream().readAllBytes();
    final String hash = requestHash(request, body);
    final Instant now = Instant.now(clock);

    final Optional<IdempotencyRecord> existing = store.find(principal.get(), key);
    if (existing.isPresent()) {
      final IdempotencyRecord record = existing.get();
      if (record.isExpired(now)) {
        store.release(record.getId());
      } else {
        answerExisting(record, hash, request, response);
        return;
      }
    }

    final IdempotencyRecord reservation =
        IdempotencyRecord.reserve(
            principal.get(), key, request.getMethod(), request.getRequestURI(), hash, now, now.plus(ttl));
    if (!store.tryReserve(reservation)) {
      // A concurrent request with the same key reserved it between our find and insert.
      final Optional<IdempotencyRecord> winner = store.find(principal.get(), key);
      if (winner.isPresent()) {
        answerExisting(winner.get(), hash, request, response);
      } else {
        count("in_progress");
        writeInProgress(request, response);
      }
      return;
    }

    execute(reservation, new CachedBodyRequest(request, body), response, chain);
  }

  private void execute(
      final IdempotencyRecord reservation,
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain)
      throws IOException, ServletException {
    final ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
    try {
      chain.doFilter(request, wrapped);
    } catch (IOException | ServletException | RuntimeException ex) {
      store.release(reservation.getId());
      throw ex;
    }
    final int status = wrapped.getStatus();
    if (status >= 500) {
      store.release(reservation.getId());
      count("released");
    } else {
      final byte[] responseBody = wrapped.getContentAsByteArray();
      store.complete(
          reservation.getId(),
          status,
          wrapped.getContentType(),
          responseBody.length == 0 ? null : new String(responseBody, StandardCharsets.UTF_8));
      count("executed");
    }
    wrapped.copyBodyToResponse();
  }

  private void answerExisting(
      final IdempotencyRecord record,
      final String hash,
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws IOException {
    if (!record.getRequestHash().equals(hash)) {
      count("mismatch");
      writeProblem(request, response, HttpStatus.UNPROCESSABLE_CONTENT, "idempotency.error.payload.mismatch");
      return;
    }
    if (record.getStatus() == IdempotencyRecord.Status.IN_PROGRESS) {
      count("in_progress");
      writeInProgress(request, response);
      return;
    }
    count("replayed");
    log.atInfo()
        .addKeyValue("event", "idempotency.replay")
        .addKeyValue("method", record.getMethod())
        .addKeyValue("path", record.getPath())
        .addKeyValue("status", record.getResponseStatus())
        .log("idempotency.replay");
    response.setStatus(record.getResponseStatus());
    response.setHeader(REPLAYED_HEADER, "true");
    if (record.getResponseContentType() != null) {
      response.setContentType(record.getResponseContentType());
    }
    if (record.getResponseBody() != null) {
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.getWriter().write(record.getResponseBody());
    }
  }

  private void writeInProgress(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {
    response.setHeader(STATUS_HEADER, "in-progress");
    writeProblem(request, response, HttpStatus.CONFLICT, "idempotency.error.in.progress");
  }

  private void writeProblem(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final HttpStatus status,
      final String code)
      throws IOException {
    final ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setTitle(messages.getMessage("idempotency.error.title"));
    problem.setDetail(messages.getMessage(code));
    problem.setProperty("code", code);
    final Object traceId = request.getAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE);
    if (traceId != null) {
      problem.setProperty("traceId", traceId);
    }
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), problem);
  }

  private void count(final String outcome) {
    Counter.builder("idempotency.requests")
        .description("Mutating requests carrying an Idempotency-Key, by outcome")
        .tag("outcome", outcome)
        .register(meterRegistry)
        .increment();
  }

  private static Optional<String> currentPrincipal() {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return Optional.empty();
    }
    return Optional.ofNullable(StringUtils.trimToNull(auth.getName()));
  }

  /** SHA-256 of method, path, query string and body: what "the same request" means. */
  static String requestHash(final HttpServletRequest request, final byte[] body) {
    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(request.getMethod().toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
      digest.update((byte) '\n');
      digest.update(request.getRequestURI().getBytes(StandardCharsets.UTF_8));
      digest.update((byte) '?');
      digest.update(StringUtils.defaultString(request.getQueryString()).getBytes(StandardCharsets.UTF_8));
      digest.update((byte) '\n');
      digest.update(body);
      return HexFormat.of().formatHex(digest.digest());
    } catch (final NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 not available", ex);
    }
  }

  /** Request wrapper that replays a body already consumed for hashing. */
  static final class CachedBodyRequest extends HttpServletRequestWrapper {

    private final byte[] body;

    CachedBodyRequest(final HttpServletRequest request, final byte[] body) {
      super(request);
      this.body = body;
    }

    @Override
    public ServletInputStream getInputStream() {
      final ByteArrayInputStream source = new ByteArrayInputStream(body);
      return new ServletInputStream() {
        @Override
        public boolean isFinished() {
          return source.available() == 0;
        }

        @Override
        public boolean isReady() {
          return true;
        }

        @Override
        public void setReadListener(final ReadListener listener) {
          throw new UnsupportedOperationException("Async reads are not supported");
        }

        @Override
        public int read() {
          return source.read();
        }

        @Override
        public int read(final byte[] buffer, final int offset, final int length) {
          return source.read(buffer, offset, length);
        }
      };
    }

    @Override
    public BufferedReader getReader() {
      return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public int getContentLength() {
      return body.length;
    }

    @Override
    public long getContentLengthLong() {
      return body.length;
    }
  }
}
