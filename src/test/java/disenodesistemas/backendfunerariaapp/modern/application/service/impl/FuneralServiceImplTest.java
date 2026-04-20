package disenodesistemas.backendfunerariaapp.modern.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.service.impl.FuneralServiceImpl;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.util.List;
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
@DisplayName("FuneralServiceImpl")
class FuneralServiceImplTest {

  @Mock private FuneralCommandUseCase funeralCommandUseCase;
  @Mock private FuneralQueryUseCase funeralQueryUseCase;

  @InjectMocks private FuneralServiceImpl funeralService;

  @Test
  @DisplayName(
      "Given a funeral request when create and update are invoked then it delegates both commands to the command use case")
  void givenAFuneralRequestWhenCreateAndUpdateAreInvokedThenItDelegatesBothCommandsToTheCommandUseCase() {
    final FuneralRequestDto request = FuneralRequestDto.builder().build();
    final FuneralResponseDto expected =
        new FuneralResponseDto(1L, null, null, "REC-123", "SER-001", null, null, null, null, null);

    when(funeralCommandUseCase.create(request)).thenReturn(expected);
    when(funeralCommandUseCase.update(1L, request)).thenReturn(expected);

    assertThat(funeralService.create(request)).isEqualTo(expected);
    assertThat(funeralService.update(1L, request)).isEqualTo(expected);
    verify(funeralCommandUseCase).create(request);
    verify(funeralCommandUseCase).update(1L, request);
  }

  @Test
  @DisplayName(
      "Given a funeral query when findAll, findById and findFuneralsByUser are invoked then it delegates the reads to the query use case")
  void givenAFuneralQueryWhenFindAllFindByIdAndFindFuneralsByUserAreInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final FuneralResponseDto response =
        new FuneralResponseDto(1L, null, null, "REC-123", "SER-001", null, null, null, null, null);
    final List<FuneralResponseDto> responses = List.of(response);

    when(funeralQueryUseCase.findAll()).thenReturn(responses);
    when(funeralQueryUseCase.findById(1L)).thenReturn(response);
    when(funeralQueryUseCase.findFuneralsByUser()).thenReturn(responses);

    assertThat(funeralService.findAll()).isEqualTo(responses);
    assertThat(funeralService.findById(1L)).isEqualTo(response);
    assertThat(funeralService.findFuneralsByUser()).isEqualTo(responses);
    verify(funeralQueryUseCase).findAll();
    verify(funeralQueryUseCase).findById(1L);
    verify(funeralQueryUseCase).findFuneralsByUser();
  }

  @Test
  @DisplayName(
      "Given a funeral identifier when delete is invoked then it delegates the command to the command use case")
  void givenAFuneralIdentifierWhenDeleteIsInvokedThenItDelegatesTheCommandToTheCommandUseCase() {
    funeralService.delete(1L);

    verify(funeralCommandUseCase).delete(1L);
  }
}
