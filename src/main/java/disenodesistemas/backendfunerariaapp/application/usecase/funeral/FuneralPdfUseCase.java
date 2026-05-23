package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPdfRenderPort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Looks up a funeral by id and asks the {@link FuneralPdfRenderPort} to produce
 * a printable PDF. Pure orchestration — the actual layout lives in the
 * infrastructure adapter so the use case stays free of OpenPDF (or whichever
 * library is in use) and can be exercised with a plain Mockito stub.
 *
 * <p>The lookup reuses the same persistence port + "funeral.error.not.found"
 * key as {@link FuneralQueryUseCase}, so a missing id surfaces the same 404
 * Problem Details the rest of the API already returns.
 */
@Service
@RequiredArgsConstructor
public class FuneralPdfUseCase {

  private final FuneralPersistencePort funeralPersistencePort;
  private final FuneralPdfRenderPort funeralPdfRenderPort;

  /**
   * Generates the PDF for the given funeral.
   *
   * @param id the funeral id to render.
   * @return the encoded PDF bytes.
   * @throws NotFoundException when no funeral matches the given id.
   * @throws FuneralPdfRenderPort.PdfRenderException when the renderer fails.
   */
  @Transactional(readOnly = true)
  public byte[] generatePdf(final Long id) {
    final Funeral funeral =
        funeralPersistencePort
            .findById(id)
            .orElseThrow(() -> new NotFoundException("funeral.error.not.found"));
    return funeralPdfRenderPort.render(funeral);
  }
}
