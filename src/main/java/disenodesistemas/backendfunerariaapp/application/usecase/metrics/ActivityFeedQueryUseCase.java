package disenodesistemas.backendfunerariaapp.application.usecase.metrics;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityFeedReadPort;
import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import disenodesistemas.backendfunerariaapp.web.dto.response.ActivityFeedEntryDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ActivityFeedResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads the {@code activity_log} read model (ADR-0014) into the wire-format DTO consumed by
 * the dashboard's recent-activity panel. Pure projection; no business rules.
 *
 * <h3>Limit handling</h3>
 *
 * The caller-supplied {@code limit} is clamped between {@link #MIN_LIMIT} and
 * {@link #MAX_LIMIT}. The upper bound prevents an operator from accidentally pulling the
 * whole table; the lower bound rejects zero (which would always return an empty list and
 * mask a typo at the call site).
 */
@Service
public class ActivityFeedQueryUseCase {

  public static final int MIN_LIMIT = 1;
  public static final int MAX_LIMIT = 100;
  public static final int DEFAULT_LIMIT = 20;

  private final ActivityFeedReadPort readPort;

  public ActivityFeedQueryUseCase(final ActivityFeedReadPort readPort) {
    this.readPort = readPort;
  }

  /**
   * Returns the most recent {@code limit} activity entries, newest first. {@code limit} is
   * clamped to the configured range; a value below {@link #MIN_LIMIT} is rounded up, a value
   * above {@link #MAX_LIMIT} is capped down.
   */
  @Transactional(readOnly = true)
  public ActivityFeedResponseDto getRecentActivity(final int limit) {
    final int clamped = Math.min(MAX_LIMIT, Math.max(MIN_LIMIT, limit));
    final List<ActivityFeedEntryDto> entries =
        readPort.findLatest(clamped).stream().map(ActivityFeedQueryUseCase::toDto).toList();
    return new ActivityFeedResponseDto(entries);
  }

  private static ActivityFeedEntryDto toDto(final ActivityLogEntry row) {
    return new ActivityFeedEntryDto(
        row.getEventId(),
        row.getEventType(),
        row.getAggregateType(),
        row.getAggregateId(),
        row.getSummary(),
        row.getOccurredAt());
  }
}
