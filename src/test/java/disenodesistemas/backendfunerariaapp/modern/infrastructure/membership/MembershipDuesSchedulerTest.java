package disenodesistemas.backendfunerariaapp.modern.infrastructure.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipDuesUseCase;
import disenodesistemas.backendfunerariaapp.infrastructure.membership.MembershipDuesScheduler;
import disenodesistemas.backendfunerariaapp.web.dto.response.DuesGenerationResultDto;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipDuesSchedulerTest {

  @Mock private MembershipDuesUseCase duesUseCase;

  @Test
  @DisplayName("Given a successful run when the scheduler fires then created dues are counted")
  void countsCreatedDues() {
    final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    when(duesUseCase.generateCurrentMonth())
        .thenReturn(new DuesGenerationResultDto(YearMonth.of(2026, 11), 3, 1, 1, 0));

    new MembershipDuesScheduler(duesUseCase, registry).generateCurrentMonth();

    assertThat(registry.get("membership.dues.created").counter().count()).isEqualTo(3.0);
    assertThat(registry.get("membership.dues.runs").counter().count()).isZero();
  }

  @Test
  @DisplayName("Given a failing run when the scheduler fires then the failure is counted and swallowed")
  void countsFailures() {
    final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    when(duesUseCase.generateCurrentMonth()).thenThrow(new IllegalStateException("db down"));

    new MembershipDuesScheduler(duesUseCase, registry).generateCurrentMonth();

    assertThat(registry.get("membership.dues.runs").counter().count()).isEqualTo(1.0);
  }
}
