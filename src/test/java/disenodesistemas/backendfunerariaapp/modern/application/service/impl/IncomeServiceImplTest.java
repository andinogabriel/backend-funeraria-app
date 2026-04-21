package disenodesistemas.backendfunerariaapp.modern.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.service.impl.IncomeServiceImpl;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("IncomeServiceImpl")
class IncomeServiceImplTest {

  @Mock private IncomeCommandUseCase incomeCommandUseCase;
  @Mock private IncomeQueryUseCase incomeQueryUseCase;

  @InjectMocks private IncomeServiceImpl incomeService;

  @Test
  @DisplayName(
      "Given an income request when create and update are invoked then it delegates both commands to the command use case")
  void givenAnIncomeRequestWhenCreateAndUpdateAreInvokedThenItDelegatesBothCommandsToTheCommandUseCase() {
    final IncomeRequestDto request =
        IncomeRequestDto.builder()
            .receiptNumber(7002L)
            .receiptSeries(1001L)
            .tax(new BigDecimal("21.00"))
            .incomeUser(UserDto.builder().email("john.doe@example.com").build())
            .incomeDetails(List.of())
            .build();
    final IncomeResponseDto expected =
        new IncomeResponseDto("7002", "1001", null, null, new BigDecimal("21.00"), new BigDecimal("242.00"), null, null, null, null, List.of());

    when(incomeCommandUseCase.create(request)).thenReturn(expected);
    when(incomeCommandUseCase.update(7002L, request)).thenReturn(expected);

    assertThat(incomeService.create(request)).isEqualTo(expected);
    assertThat(incomeService.update(7002L, request)).isEqualTo(expected);
    verify(incomeCommandUseCase).create(request);
    verify(incomeCommandUseCase).update(7002L, request);
  }

  @Test
  @DisplayName(
      "Given an income query when findAll, findById and findByReceiptNumber are invoked then it delegates the reads to the query use case")
  void givenAnIncomeQueryWhenFindAllFindByIdAndFindByReceiptNumberAreInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final IncomeResponseDto response =
        new IncomeResponseDto("7002", "1001", null, null, new BigDecimal("21.00"), new BigDecimal("242.00"), null, null, null, null, List.of());
    final List<IncomeResponseDto> responses = List.of(response);

    when(incomeQueryUseCase.findAll()).thenReturn(responses);
    when(incomeQueryUseCase.findById(7002L)).thenReturn(response);
    when(incomeQueryUseCase.findByReceiptNumber(7002L)).thenReturn(response);

    assertThat(incomeService.findAll()).isEqualTo(responses);
    assertThat(incomeService.findById(7002L)).isEqualTo(response);
    assertThat(incomeService.findByReceiptNumber(7002L)).isEqualTo(response);
    verify(incomeQueryUseCase).findAll();
    verify(incomeQueryUseCase).findById(7002L);
    verify(incomeQueryUseCase).findByReceiptNumber(7002L);
  }

  @Test
  @DisplayName(
      "Given a pagination request when getIncomesPaginated is invoked then it delegates the read to the query use case")
  void givenAPaginationRequestWhenGetIncomesPaginatedIsInvokedThenItDelegatesTheReadToTheQueryUseCase() {
    final Page<IncomeResponseDto> expected =
        new PageImpl<>(
            List.of(
                new IncomeResponseDto(
                    "7002",
                    "1001",
                    null,
                    null,
                    new BigDecimal("21.00"),
                    new BigDecimal("242.00"),
                    null,
                    null,
                    null,
                    null,
                    List.of())));

    when(incomeQueryUseCase.getIncomesPaginated(false, 1, 10, "receiptNumber", "desc"))
        .thenReturn(expected);

    final Page<IncomeResponseDto> response =
        incomeService.getIncomesPaginated(false, 1, 10, "receiptNumber", "desc");

    assertThat(response).isEqualTo(expected);
    verify(incomeQueryUseCase).getIncomesPaginated(false, 1, 10, "receiptNumber", "desc");
  }

  @Test
  @DisplayName(
      "Given an income identifier when delete is invoked then it delegates the command to the command use case")
  void givenAnIncomeIdentifierWhenDeleteIsInvokedThenItDelegatesTheCommandToTheCommandUseCase() {
    incomeService.delete(7002L);

    verify(incomeCommandUseCase).delete(7002L);
  }
}
