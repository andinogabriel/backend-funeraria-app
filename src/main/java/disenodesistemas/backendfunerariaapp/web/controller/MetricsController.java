package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.metrics.ActivityFeedQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.metrics.DashboardMetricsQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.ActivityFeedResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DashboardMetricsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only metrics surface that powers the operator dashboard's KPI bento + recent-activity
 * feed.
 *
 * <p>Gated to authenticated sessions (admin or user). The dashboard endpoint is a single
 * aggregated snapshot rather than four separate calls — every dashboard load issues one
 * round-trip, and the four counts live close together in time so a partial response would
 * give a confusing UX (e.g. afiliados from second 0 next to servicios from second 3). The
 * activity feed is a separate endpoint because it ships an arbitrarily-sized list and the two
 * have different cacheability profiles.
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

  private final DashboardMetricsQueryUseCase dashboardMetricsQueryUseCase;
  private final ActivityFeedQueryUseCase activityFeedQueryUseCase;

  /**
   * Returns the current snapshot of dashboard KPIs (afiliados activos, planes activos,
   * servicios del mes, eventos auditados en las últimas 24 h) with sparkline series and a
   * trend percentage where computable.
   */
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/dashboard")
  public ResponseEntity<DashboardMetricsResponseDto> dashboard() {
    return ResponseEntity.ok(dashboardMetricsQueryUseCase.buildSnapshot());
  }

  /**
   * Returns the most recent activity entries projected from the outbox event stream
   * (ADR-0014). The optional {@code limit} parameter defaults to
   * {@link ActivityFeedQueryUseCase#DEFAULT_LIMIT} and is clamped to
   * {@code [MIN_LIMIT, MAX_LIMIT]} server-side, so a misbehaving client cannot drag down the
   * read model by requesting an unbounded page.
   */
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/activity-feed")
  public ResponseEntity<ActivityFeedResponseDto> activityFeed(
      @RequestParam(name = "limit", required = false) final Integer limit) {
    final int effective = limit == null ? ActivityFeedQueryUseCase.DEFAULT_LIMIT : limit;
    return ResponseEntity.ok(activityFeedQueryUseCase.getRecentActivity(effective));
  }
}
