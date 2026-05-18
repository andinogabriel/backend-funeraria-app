package disenodesistemas.backendfunerariaapp.modern.application.usecase.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityFeedReadPort;
import disenodesistemas.backendfunerariaapp.application.usecase.metrics.ActivityFeedQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import disenodesistemas.backendfunerariaapp.web.dto.response.ActivityFeedResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ActivityFeedQueryUseCase")
class ActivityFeedQueryUseCaseTest {

  @Test
  @DisplayName("forwards the supplied limit when it sits inside the clamp range")
  void honoursLimitInsideRange() {
    final ActivityFeedReadPort readPort = mock(ActivityFeedReadPort.class);
    when(readPort.findLatest(anyInt())).thenReturn(List.of());

    new ActivityFeedQueryUseCase(readPort).getRecentActivity(15);

    verify(readPort).findLatest(eq(15));
  }

  @Test
  @DisplayName("clamps a limit above MAX_LIMIT down to MAX_LIMIT")
  void clampsAboveMax() {
    final ActivityFeedReadPort readPort = mock(ActivityFeedReadPort.class);
    when(readPort.findLatest(anyInt())).thenReturn(List.of());

    new ActivityFeedQueryUseCase(readPort).getRecentActivity(10_000);

    verify(readPort).findLatest(eq(ActivityFeedQueryUseCase.MAX_LIMIT));
  }

  @Test
  @DisplayName("clamps a limit below MIN_LIMIT up to MIN_LIMIT")
  void clampsBelowMin() {
    final ActivityFeedReadPort readPort = mock(ActivityFeedReadPort.class);
    when(readPort.findLatest(anyInt())).thenReturn(List.of());

    new ActivityFeedQueryUseCase(readPort).getRecentActivity(0);

    verify(readPort).findLatest(eq(ActivityFeedQueryUseCase.MIN_LIMIT));
  }

  @Test
  @DisplayName("maps every ActivityLogEntry row to its wire DTO without reordering")
  void mapsRowsToDtoInOrder() {
    final ActivityFeedReadPort readPort = mock(ActivityFeedReadPort.class);
    final UUID firstId = UUID.randomUUID();
    final UUID secondId = UUID.randomUUID();
    when(readPort.findLatest(anyInt()))
        .thenReturn(
            List.of(
                new ActivityLogEntry(
                    firstId,
                    "FUNERAL_CREATED",
                    "FUNERAL",
                    "1",
                    "Nuevo servicio",
                    Instant.parse("2026-05-17T12:00:00Z"),
                    "trace-1"),
                new ActivityLogEntry(
                    secondId,
                    "AFFILIATE_DELETED",
                    "AFFILIATE",
                    "12345678",
                    "Afiliado eliminado",
                    Instant.parse("2026-05-17T11:00:00Z"),
                    "trace-2")));

    final ActivityFeedResponseDto response =
        new ActivityFeedQueryUseCase(readPort).getRecentActivity(20);

    assertThat(response.entries()).hasSize(2);
    assertThat(response.entries().get(0).eventId()).isEqualTo(firstId);
    assertThat(response.entries().get(0).summary()).isEqualTo("Nuevo servicio");
    assertThat(response.entries().get(1).eventType()).isEqualTo("AFFILIATE_DELETED");
  }
}
