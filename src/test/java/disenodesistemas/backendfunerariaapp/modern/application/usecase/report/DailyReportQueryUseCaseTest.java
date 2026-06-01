package disenodesistemas.backendfunerariaapp.modern.application.usecase.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.usecase.report.DailyReportQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.report.MonetaryAggregate;
import disenodesistemas.backendfunerariaapp.web.dto.response.DailyReportResponseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Unit coverage for the orchestration: the use case wires three JDBC aggregations into the response
 * and computes {@code net = services.total - purchases.total}. The SQL itself is covered by {@code
 * DailyReportQueryUseCasePostgresIntegrationTest}; here the {@link JdbcTemplate} is mocked.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class DailyReportQueryUseCaseTest {

  private static final LocalDate DAY = LocalDate.of(2026, 5, 30);

  @Mock private JdbcTemplate jdbcTemplate;

  /**
   * Stubs the two {@link RowMapper} queries (services first, then active purchases — matching the
   * call order in the use case) and the {@code Long.class} query (annulled count).
   */
  @SuppressWarnings("unchecked")
  private void stub(
      final MonetaryAggregate services,
      final MonetaryAggregate activePurchases,
      final long annulled) {
    when(jdbcTemplate.queryForObject(any(String.class), any(RowMapper.class), any(), any()))
        .thenReturn(services, activePurchases);
    when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), any(), any()))
        .thenReturn(annulled);
  }

  @Test
  @DisplayName("Given services above purchases, then net is their positive difference")
  void net_is_services_total_minus_purchases_total() {
    stub(
        new MonetaryAggregate(3, new BigDecimal("1500000.00")),
        new MonetaryAggregate(2, new BigDecimal("420000.00")),
        0L);

    final DailyReportResponseDto report =
        new DailyReportQueryUseCase(jdbcTemplate).buildDailyReport(DAY);

    assertThat(report.date()).isEqualTo(DAY);
    assertThat(report.services().count()).isEqualTo(3);
    assertThat(report.services().total()).isEqualByComparingTo("1500000.00");
    assertThat(report.purchases().count()).isEqualTo(2);
    assertThat(report.purchases().total()).isEqualByComparingTo("420000.00");
    assertThat(report.purchases().annulledCount()).isZero();
    assertThat(report.net()).isEqualByComparingTo("1080000.00");
  }

  @Test
  @DisplayName("Given purchases above services, then net is negative and annulledCount surfaces")
  void net_is_negative_when_purchases_exceed_services() {
    stub(
        new MonetaryAggregate(0, BigDecimal.ZERO),
        new MonetaryAggregate(1, new BigDecimal("90000.00")),
        1L);

    final DailyReportResponseDto report =
        new DailyReportQueryUseCase(jdbcTemplate).buildDailyReport(DAY);

    assertThat(report.purchases().annulledCount()).isEqualTo(1);
    assertThat(report.net()).isEqualByComparingTo("-90000.00");
  }

  @Test
  @DisplayName("Given an empty day, then net is zero")
  void empty_day_nets_to_zero() {
    stub(new MonetaryAggregate(0, BigDecimal.ZERO), new MonetaryAggregate(0, BigDecimal.ZERO), 0L);

    final DailyReportResponseDto report =
        new DailyReportQueryUseCase(jdbcTemplate).buildDailyReport(DAY);

    assertThat(report.net()).isEqualByComparingTo("0");
  }
}
