package disenodesistemas.backendfunerariaapp.modern.application.usecase.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityLogRetentionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxRetentionPort;
import disenodesistemas.backendfunerariaapp.application.usecase.retention.RetentionUseCase;
import disenodesistemas.backendfunerariaapp.config.RetentionProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit coverage for {@link RetentionUseCase}. The persistence-bound transitions live in
 * {@code RetentionPostgresIntegrationTest}; this class pins the cutoff arithmetic, the
 * disabled short-circuit, the batch loop's stop-on-zero behaviour and the safety cap.
 */
@DisplayName("RetentionUseCase")
class RetentionUseCaseTest {

  private static final Instant NOW = Instant.parse("2026-05-19T03:30:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private static RetentionProperties props(
      final boolean enabled,
      final int batchSize,
      final int maxBatches,
      final int outboxSoftDays,
      final int outboxHardDays,
      final int activitySoftDays,
      final int activityHardDays) {
    return new RetentionProperties(
        enabled,
        batchSize,
        maxBatches,
        new RetentionProperties.Window(outboxSoftDays, outboxHardDays),
        new RetentionProperties.Window(activitySoftDays, activityHardDays));
  }

  @Test
  @DisplayName("When the feature flag is off then no port is touched and the result reports zeros")
  void disabledShortCircuits() {
    final OutboxRetentionPort outbox = mock(OutboxRetentionPort.class);
    final ActivityLogRetentionPort activity = mock(ActivityLogRetentionPort.class);
    final RetentionUseCase useCase =
        new RetentionUseCase(
            outbox, activity, props(false, 100, 10, 30, 60, 90, 90), FIXED_CLOCK);

    final RetentionUseCase.RetentionResult result = useCase.runOnce();

    assertThat(result.outboxSoftDeleted()).isZero();
    assertThat(result.outboxHardDeleted()).isZero();
    assertThat(result.activitySoftDeleted()).isZero();
    assertThat(result.activityHardDeleted()).isZero();
    verifyNoInteractions(outbox, activity);
  }

  @Test
  @DisplayName(
      "Cutoffs are derived from the fixed clock minus the configured day counts and forwarded to every port call")
  void cutoffsAreFixedClockMinusWindow() {
    final OutboxRetentionPort outbox = mock(OutboxRetentionPort.class);
    final ActivityLogRetentionPort activity = mock(ActivityLogRetentionPort.class);
    when(outbox.softDeletePublishedBefore(
            eq(NOW.minusSeconds(30L * 86400L)), eq(NOW), eq(1000)))
        .thenReturn(0); // single empty batch ends the phase immediately
    when(outbox.hardDeleteSoftDeletedBefore(eq(NOW.minusSeconds(60L * 86400L)), eq(1000)))
        .thenReturn(0);
    when(activity.softDeleteOccurredBefore(
            eq(NOW.minusSeconds(90L * 86400L)), eq(NOW), eq(1000)))
        .thenReturn(0);
    when(activity.hardDeleteSoftDeletedBefore(eq(NOW.minusSeconds(90L * 86400L)), eq(1000)))
        .thenReturn(0);

    new RetentionUseCase(
            outbox, activity, props(true, 1000, 10, 30, 60, 90, 90), FIXED_CLOCK)
        .runOnce();

    verify(outbox).softDeletePublishedBefore(NOW.minusSeconds(30L * 86400L), NOW, 1000);
    verify(outbox).hardDeleteSoftDeletedBefore(NOW.minusSeconds(60L * 86400L), 1000);
    verify(activity).softDeleteOccurredBefore(NOW.minusSeconds(90L * 86400L), NOW, 1000);
    verify(activity).hardDeleteSoftDeletedBefore(NOW.minusSeconds(90L * 86400L), 1000);
  }

  @Test
  @DisplayName("A phase loops over batches and stops the moment a batch returns zero affected rows")
  void phaseLoopsUntilZero() {
    final OutboxRetentionPort outbox = mock(OutboxRetentionPort.class);
    final ActivityLogRetentionPort activity = mock(ActivityLogRetentionPort.class);
    // First batch wipes 1000, second 1000, third 250, fourth 0 → loop stops at fourth.
    when(outbox.softDeletePublishedBefore(eq(NOW.minusSeconds(30L * 86400L)), eq(NOW), eq(1000)))
        .thenReturn(1000, 1000, 250, 0);

    final RetentionUseCase.RetentionResult result =
        new RetentionUseCase(
                outbox, activity, props(true, 1000, 50, 30, 60, 90, 90), FIXED_CLOCK)
            .runOnce();

    assertThat(result.outboxSoftDeleted()).isEqualTo(2250);
    verify(outbox, times(4))
        .softDeletePublishedBefore(NOW.minusSeconds(30L * 86400L), NOW, 1000);
  }

  @Test
  @DisplayName(
      "The safety cap stops the loop after maxBatchesPerRun even if the port keeps returning rows")
  void safetyCapStopsRunawayLoop() {
    final OutboxRetentionPort outbox = mock(OutboxRetentionPort.class);
    final ActivityLogRetentionPort activity = mock(ActivityLogRetentionPort.class);
    when(outbox.softDeletePublishedBefore(eq(NOW.minusSeconds(30L * 86400L)), eq(NOW), eq(1000)))
        .thenReturn(1000); // every batch reports work — would loop forever without the cap

    final RetentionUseCase.RetentionResult result =
        new RetentionUseCase(
                outbox, activity, props(true, 1000, 3, 30, 60, 90, 90), FIXED_CLOCK)
            .runOnce();

    // Cap=3 → exactly 3 batches × 1000 rows = 3000 affected.
    assertThat(result.outboxSoftDeleted()).isEqualTo(3000);
    verify(outbox, times(3))
        .softDeletePublishedBefore(NOW.minusSeconds(30L * 86400L), NOW, 1000);
    // The other three phases still receive their first batch (they each return 0
    // immediately because the mock isn't stubbed for them, defaulting to int = 0).
    verify(outbox, times(1)).hardDeleteSoftDeletedBefore(NOW.minusSeconds(60L * 86400L), 1000);
    verify(activity, times(1))
        .softDeleteOccurredBefore(NOW.minusSeconds(90L * 86400L), NOW, 1000);
    verify(activity, times(1)).hardDeleteSoftDeletedBefore(NOW.minusSeconds(90L * 86400L), 1000);
  }

  @Test
  @DisplayName("The four phases are invoked in the documented order: outbox soft → outbox hard → activity soft → activity hard")
  void phasesRunInDocumentedOrder() {
    final OutboxRetentionPort outbox = mock(OutboxRetentionPort.class);
    final ActivityLogRetentionPort activity = mock(ActivityLogRetentionPort.class);
    when(outbox.softDeletePublishedBefore(eq(NOW.minusSeconds(30L * 86400L)), eq(NOW), eq(1000)))
        .thenReturn(0);
    when(outbox.hardDeleteSoftDeletedBefore(eq(NOW.minusSeconds(60L * 86400L)), eq(1000)))
        .thenReturn(0);
    when(activity.softDeleteOccurredBefore(
            eq(NOW.minusSeconds(90L * 86400L)), eq(NOW), eq(1000)))
        .thenReturn(0);
    when(activity.hardDeleteSoftDeletedBefore(eq(NOW.minusSeconds(90L * 86400L)), eq(1000)))
        .thenReturn(0);

    final var inOrder = org.mockito.Mockito.inOrder(outbox, activity);
    new RetentionUseCase(
            outbox, activity, props(true, 1000, 10, 30, 60, 90, 90), FIXED_CLOCK)
        .runOnce();

    inOrder.verify(outbox).softDeletePublishedBefore(NOW.minusSeconds(30L * 86400L), NOW, 1000);
    inOrder.verify(outbox).hardDeleteSoftDeletedBefore(NOW.minusSeconds(60L * 86400L), 1000);
    inOrder
        .verify(activity)
        .softDeleteOccurredBefore(NOW.minusSeconds(90L * 86400L), NOW, 1000);
    inOrder
        .verify(activity)
        .hardDeleteSoftDeletedBefore(NOW.minusSeconds(90L * 86400L), 1000);
    inOrder.verifyNoMoreInteractions();
    verify(outbox, never()).softDeletePublishedBefore(NOW, NOW, 1000); // sanity guard
  }
}
