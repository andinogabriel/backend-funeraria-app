package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.metrics.DashboardMetricsQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.DashboardMetricsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only metrics surface that powers the operator dashboard's KPI bento.
 *
 * <p>Gated to authenticated sessions (admin or user). The endpoint is intentionally a single
 * aggregated snapshot rather than four separate calls — every dashboard load issues one
 * round-trip, and the four counts live close together in time so a partial response would
 * give a confusing UX (e.g. afiliados from second 0 next to servicios from second 3).
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

  private final DashboardMetricsQueryUseCase dashboardMetricsQueryUseCase;

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
}
