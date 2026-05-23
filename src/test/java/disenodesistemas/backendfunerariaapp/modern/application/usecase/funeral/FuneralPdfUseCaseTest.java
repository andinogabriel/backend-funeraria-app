package disenodesistemas.backendfunerariaapp.modern.application.usecase.funeral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPdfRenderPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralPdfUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FuneralPdfUseCase}. The use case is pure orchestration:
 * load by id, hand off to the render port, return the bytes. The tests pin the
 * happy path and the 404 branch — anything more would assert on the renderer
 * itself, which lives in the infrastructure module.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FuneralPdfUseCase")
class FuneralPdfUseCaseTest {

  @Mock private FuneralPersistencePort funeralPersistencePort;
  @Mock private FuneralPdfRenderPort funeralPdfRenderPort;
  @Mock private Funeral funeral;

  @InjectMocks private FuneralPdfUseCase useCase;

  @Test
  @DisplayName("delegates the loaded funeral to the renderer and returns its bytes")
  void delegatesToRenderer() {
    final byte[] expected = new byte[] {0x25, 0x50, 0x44, 0x46}; // %PDF magic
    when(funeralPersistencePort.findById(42L)).thenReturn(Optional.of(funeral));
    when(funeralPdfRenderPort.render(funeral)).thenReturn(expected);

    final byte[] actual = useCase.generatePdf(42L);

    assertThat(actual).isSameAs(expected);
    verify(funeralPdfRenderPort).render(funeral);
  }

  @Test
  @DisplayName("throws NotFoundException when the id does not resolve to a funeral")
  void throwsNotFound() {
    when(funeralPersistencePort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.generatePdf(99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("funeral.error.not.found");

    verifyNoInteractions(funeralPdfRenderPort);
  }
}
