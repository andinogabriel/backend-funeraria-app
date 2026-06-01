package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.report.DailyReportQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.DailyReportResponseDto;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only financial reporting surface. Admin-only — these endpoints aggregate cash figures across
 * the whole tenant and are the operator's reconciliation tooling.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

  private final DailyReportQueryUseCase dailyReportQueryUseCase;

  /**
   * Returns the daily cash reconciliation for {@code date} (ISO-8601, {@code yyyy-MM-dd}). The
   * parameter is required; Spring returns 400 when it is missing or unparseable.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/daily")
  public ResponseEntity<DailyReportResponseDto> daily(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
    return ResponseEntity.ok(dailyReportQueryUseCase.buildDailyReport(date));
  }
}
