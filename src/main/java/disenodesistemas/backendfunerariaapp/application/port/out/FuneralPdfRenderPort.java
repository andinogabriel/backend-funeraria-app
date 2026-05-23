package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;

/**
 * Outbound port for rendering a funeral service record as a printable PDF
 * document. The single concrete adapter ships in {@code infrastructure/pdf/}.
 *
 * <p>Rendered as a port (instead of inline in the use case) so the document
 * generator stays swappable — the day we move from a programmatic PDF builder
 * to a templated one (Thymeleaf + Flying Saucer) or a third-party invoicing
 * service, the use case does not change.
 *
 * <p>The port intentionally returns raw bytes rather than streaming: the
 * document is always small (one operator-readable summary, not a giant
 * statement) and downstream callers want a {@code Content-Length} header on
 * the HTTP response.
 */
public interface FuneralPdfRenderPort {

  /**
   * Renders the given funeral as a PDF document.
   *
   * <p>Implementations may throw any unchecked exception when the underlying
   * renderer fails — the global exception handler maps unhandled
   * {@link RuntimeException}s to a 500 ProblemDetails response, which is the
   * right shape for a rendering failure (no operator action recovers from it
   * other than retry / report).
   *
   * @param funeral the source record; must be fully hydrated (deceased + plan + items).
   * @return the encoded PDF as a byte array.
   */
  byte[] render(Funeral funeral);
}
