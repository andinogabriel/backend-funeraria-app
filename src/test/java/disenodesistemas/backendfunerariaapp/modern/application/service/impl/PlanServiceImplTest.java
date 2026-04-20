package disenodesistemas.backendfunerariaapp.modern.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.service.impl.PlanServiceImpl;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("PlanServiceImpl")
class PlanServiceImplTest {

  @Mock private PlanCommandUseCase planCommandUseCase;
  @Mock private PlanQueryUseCase planQueryUseCase;

  @InjectMocks private PlanServiceImpl planService;

  @Test
  @DisplayName(
      "Given a plan request when create and update are invoked then it delegates both commands to the command use case")
  void givenAPlanRequestWhenCreateAndUpdateAreInvokedThenItDelegatesBothCommandsToTheCommandUseCase() {
    final PlanRequestDto request =
        PlanRequestDto.builder()
            .id(1L)
            .name("Plan Oro")
            .description("Cobertura completa")
            .profitPercentage(new BigDecimal("25.00"))
            .itemsPlan(Set.of())
            .build();
    final PlanResponseDto expected =
        new PlanResponseDto(1L, "Plan Oro", "Cobertura completa", null, null, new BigDecimal("25.00"), Set.of());

    when(planCommandUseCase.create(request)).thenReturn(expected);
    when(planCommandUseCase.update(1L, request)).thenReturn(expected);

    assertThat(planService.create(request)).isEqualTo(expected);
    assertThat(planService.update(1L, request)).isEqualTo(expected);
    verify(planCommandUseCase).create(request);
    verify(planCommandUseCase).update(1L, request);
  }

  @Test
  @DisplayName(
      "Given a plan query when findById, findEntityById and findAll are invoked then it delegates the reads to the query use case")
  void givenAPlanQueryWhenFindByIdFindEntityByIdAndFindAllAreInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final PlanResponseDto response =
        new PlanResponseDto(1L, "Plan Oro", "Cobertura completa", null, null, new BigDecimal("25.00"), Set.of());
    final Plan entity = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    final List<PlanResponseDto> responses = List.of(response);

    when(planQueryUseCase.findById(1L)).thenReturn(response);
    when(planQueryUseCase.findEntityById(1L)).thenReturn(entity);
    when(planQueryUseCase.findAll()).thenReturn(responses);

    assertThat(planService.findById(1L)).isEqualTo(response);
    assertThat(planService.findEntityById(1L)).isEqualTo(entity);
    assertThat(planService.findAll()).isEqualTo(responses);
    verify(planQueryUseCase).findById(1L);
    verify(planQueryUseCase).findEntityById(1L);
    verify(planQueryUseCase).findAll();
  }

  @Test
  @DisplayName(
      "Given a plan identifier when delete is invoked then it delegates the command to the command use case")
  void givenAPlanIdentifierWhenDeleteIsInvokedThenItDelegatesTheCommandToTheCommandUseCase() {
    planService.delete(1L);

    verify(planCommandUseCase).delete(1L);
  }
}
