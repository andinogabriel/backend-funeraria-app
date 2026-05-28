package disenodesistemas.backendfunerariaapp.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centralizes the {@link Clock} the application uses for every wall-clock read.
 *
 * <h3>Why a bean instead of {@code Clock.systemUTC()} sprinkled in constructors</h3>
 *
 * Use cases that stamp tombstones, build audit timestamps, or compute "now"-bound
 * windows need a deterministic clock under test (frozen at a known instant so the
 * resulting JSON / DB row is byte-for-byte verifiable). The legacy pattern in this
 * codebase ({@code AffiliateCommandUseCase}, {@code DashboardMetricsQueryUseCase},
 * {@code RetentionUseCase}) carries a second constructor that takes an explicit
 * {@code Clock} for tests and defaults to {@link Clock#systemUTC()} via
 * {@code @Autowired}. The pattern works but doubles the boilerplate on every use
 * case and forces each new collaborator to remember to add the same dual ctor.
 *
 * <p>Exposing the clock as a single Spring bean keeps every consumer to one
 * {@code @RequiredArgsConstructor}-friendly final field. Tests Mockito-mock the
 * {@code Clock} or pass {@link Clock#fixed} through the standard constructor; no
 * use case has to know about the wiring decision.
 *
 * <p>The default is {@link Clock#systemUTC()} on purpose: the application stores
 * every {@code Instant} as UTC and translates to AR-local time only at the wire
 * boundary, so the system default zone never affects the persisted value.
 */
@Configuration
public class TimeConfig {

  @Bean
  public Clock systemClock() {
    return Clock.systemUTC();
  }
}
