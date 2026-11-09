package disenodesistemas.backendfunerariaapp.infrastructure.membership;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipDuesUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.DuesGenerationResultDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily {@link Scheduled} wrapper around {@link MembershipDuesUseCase#generateCurrentMonth}
 * (ADR-0021). Generation is idempotent, so running every day instead of only on the 1st means a
 * missed run (downtime on the 1st) catches up the next day, and affiliates enrolled during the
 * month are billed for it the following morning. Defaults to 04:15 in Buenos Aires, off the hour
 * and after the nightly backup; overridable through {@code app.membership.dues.cron}.
 */
@Component
@Slf4j
public class MembershipDuesScheduler {

  private final MembershipDuesUseCase duesUseCase;
  private final Counter created;
  private final Counter failedRuns;

  public MembershipDuesScheduler(
      final MembershipDuesUseCase duesUseCase, final MeterRegistry meterRegistry) {
    this.duesUseCase = duesUseCase;
    this.created =
        Counter.builder("membership.dues.created")
            .description("Membership dues created by the scheduled generator")
            .register(meterRegistry);
    this.failedRuns =
        Counter.builder("membership.dues.runs")
            .description("Scheduled dues generation runs by outcome")
            .tag("outcome", "failure")
            .register(meterRegistry);
  }

  @Scheduled(
      cron = "${app.membership.dues.cron:0 15 4 * * *}",
      zone = "America/Argentina/Buenos_Aires")
  public void generateCurrentMonth() {
    try {
      final DuesGenerationResultDto result = duesUseCase.generateCurrentMonth();
      created.increment(result.created());
      log.atInfo()
          .addKeyValue("event", "membership.dues.generated")
          .addKeyValue("period", result.period())
          .addKeyValue("created", result.created())
          .addKeyValue("repriced", result.repriced())
          .addKeyValue("unbillable", result.unbillable())
          .log("membership.dues.generated");
    } catch (final RuntimeException ex) {
      failedRuns.increment();
      log.atError()
          .addKeyValue("event", "membership.dues.failed")
          .setCause(ex)
          .log("membership.dues.failed");
    }
  }
}
