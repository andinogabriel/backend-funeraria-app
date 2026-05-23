package disenodesistemas.backendfunerariaapp.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPdfRenderPort;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Renders a {@link Funeral} as a single-page A4 portrait PDF using OpenPDF
 * (LGPL fork of iText 5).
 *
 * <h3>Layout</h3>
 *
 * The document is a four-block summary with the same field set the front-end
 * detail view shows the operator: header + receipt block + deceased block +
 * plan/items table + totals. Argentine locale on every number and date so the
 * printed copy matches what the operator was looking at on screen.
 *
 * <h3>Why programmatic instead of templated</h3>
 *
 * One document type, ~120 lines of layout code, no design churn on the
 * horizon. A templating engine (Thymeleaf + Flying Saucer) is the right tool
 * the moment we ship a second document type — until then it would be more
 * indirection than the use case warrants.
 *
 * <h3>Thread-safety</h3>
 *
 * The adapter is stateless; OpenPDF's {@code Document} / {@code PdfWriter} are
 * created per call and disposed immediately. Safe to share as a singleton
 * Spring bean.
 */
@Component
public class OpenPdfFuneralPdfRenderAdapter implements FuneralPdfRenderPort {

  /** Argentina is UTC-3 with no DST; pin the zone so the printout never drifts. */
  private static final ZoneId AR_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final Locale AR_LOCALE = Locale.forLanguageTag("es-AR");

  private static final Font TITLE_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(0x1F2937));
  private static final Font SECTION_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(0x1F2937));
  private static final Font LABEL_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(0x6B7280));
  private static final Font VALUE_FONT =
      FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(0x111827));
  private static final Font TABLE_HEADER_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
  private static final Font TABLE_CELL_FONT =
      FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(0x111827));
  private static final Font TOTAL_FONT =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(0x111827));

  private static final Color TABLE_HEADER_BG = new Color(0x1F2937);
  private static final Color TABLE_STRIPE_BG = new Color(0xF3F4F6);

  @Override
  public byte[] render(final Funeral funeral) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Document document = new Document(PageSize.A4, 48, 48, 48, 48);
    try {
      PdfWriter.getInstance(document, out);
      document.open();
      writeHeader(document, funeral);
      writeReceiptSection(document, funeral);
      writeDeceasedSection(document, funeral.getDeceased());
      writePlanSection(document, funeral.getPlan());
      writeTotals(document, funeral);
    } catch (final DocumentException e) {
      throw new IllegalStateException(
          "Failed to render funeral PDF for id=" + funeral.getId(), e);
    } finally {
      if (document.isOpen()) {
        document.close();
      }
    }
    return out.toByteArray();
  }

  /* ------------------------------ Sections ------------------------------- */

  private void writeHeader(final Document document, final Funeral funeral) throws DocumentException {
    final Paragraph title = new Paragraph("Servicio funerario", TITLE_FONT);
    title.setSpacingAfter(4);
    document.add(title);

    final Paragraph subtitle =
        new Paragraph(
            "Recibo " + nullSafe(funeral.getReceiptSeries()) + "-" + nullSafe(funeral.getReceiptNumber()),
            VALUE_FONT);
    subtitle.setSpacingAfter(18);
    document.add(subtitle);
  }

  private void writeReceiptSection(final Document document, final Funeral funeral)
      throws DocumentException {
    document.add(sectionHeading("Datos del servicio"));
    final PdfPTable table = twoColumnTable();
    appendRow(table, "Fecha y hora", formatDateTime(funeral.getFuneralDate()));
    appendRow(table, "Tipo de recibo", funeral.getReceiptType() == null
        ? "—"
        : funeral.getReceiptType().getName());
    appendRow(table, "Impuesto", funeral.getTax() == null ? "—" : funeral.getTax().toPlainString() + " %");
    appendRow(table, "Registrado", formatDateTime(funeral.getRegisterDate()));
    document.add(table);
  }

  private void writeDeceasedSection(final Document document, final DeceasedEntity deceased)
      throws DocumentException {
    document.add(sectionHeading("Fallecido"));
    final PdfPTable table = twoColumnTable();
    appendRow(table, "Nombre completo", deceased.getFirstName() + " " + deceased.getLastName());
    appendRow(table, "DNI", String.valueOf(deceased.getDni()));
    appendRow(table, "Fecha de nacimiento", formatDate(deceased.getBirthDate()));
    appendRow(table, "Fecha de fallecimiento", formatDate(deceased.getDeathDate()));
    appendRow(table, "Género", deceased.getGender() == null ? "—" : deceased.getGender().getName());
    appendRow(
        table,
        "Parentesco",
        deceased.getDeceasedRelationship() == null
            ? "—"
            : deceased.getDeceasedRelationship().getName());
    appendRow(
        table,
        "Causa de muerte",
        deceased.getDeathCause() == null ? "—" : deceased.getDeathCause().getName());
    if (deceased.getPlaceOfDeath() != null) {
      appendRow(table, "Lugar de fallecimiento", formatAddress(deceased.getPlaceOfDeath()));
    }
    document.add(table);
  }

  private void writePlanSection(final Document document, final Plan plan) throws DocumentException {
    document.add(sectionHeading("Plan e items"));
    final PdfPTable summary = twoColumnTable();
    appendRow(summary, "Plan", plan.getName());
    if (plan.getDescription() != null && !plan.getDescription().isBlank()) {
      appendRow(summary, "Descripción", plan.getDescription());
    }
    document.add(summary);

    final List<ItemPlanEntity> rows = plan.getItemsPlan().stream()
        .sorted(Comparator.comparing(row -> row.getItem().getName()))
        .toList();

    final PdfPTable itemsTable = new PdfPTable(new float[] {3.5f, 1.5f, 1f});
    itemsTable.setWidthPercentage(100);
    itemsTable.setSpacingBefore(8);
    itemsTable.setSpacingAfter(12);
    itemsTable.addCell(headerCell("Item"));
    itemsTable.addCell(headerCell("Código"));
    itemsTable.addCell(headerCell("Cantidad", Element.ALIGN_RIGHT));

    boolean stripe = false;
    for (final ItemPlanEntity row : rows) {
      final Color bg = stripe ? TABLE_STRIPE_BG : Color.WHITE;
      itemsTable.addCell(bodyCell(row.getItem().getName(), bg));
      itemsTable.addCell(bodyCell(row.getItem().getCode(), bg));
      itemsTable.addCell(bodyCell(String.valueOf(row.getQuantity()), bg, Element.ALIGN_RIGHT));
      stripe = !stripe;
    }
    document.add(itemsTable);
  }

  private void writeTotals(final Document document, final Funeral funeral) throws DocumentException {
    final PdfPTable totals = new PdfPTable(new float[] {3f, 1f});
    totals.setWidthPercentage(60);
    totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

    final PdfPCell label = new PdfPCell(new Phrase("Total", TOTAL_FONT));
    label.setBorder(0);
    label.setHorizontalAlignment(Element.ALIGN_RIGHT);
    label.setPaddingBottom(6);
    totals.addCell(label);

    final PdfPCell value = new PdfPCell(new Phrase(formatCurrency(funeral.getTotalAmount()), TOTAL_FONT));
    value.setBorder(0);
    value.setHorizontalAlignment(Element.ALIGN_RIGHT);
    value.setPaddingBottom(6);
    totals.addCell(value);

    document.add(totals);
  }

  /* ------------------------------ Helpers -------------------------------- */

  private Paragraph sectionHeading(final String text) {
    final Paragraph p = new Paragraph(text, SECTION_FONT);
    p.setSpacingBefore(8);
    p.setSpacingAfter(4);
    return p;
  }

  private PdfPTable twoColumnTable() {
    final PdfPTable table = new PdfPTable(new float[] {1f, 2.5f});
    table.setWidthPercentage(100);
    table.setSpacingAfter(8);
    return table;
  }

  private void appendRow(final PdfPTable table, final String label, final String value) {
    final PdfPCell labelCell = new PdfPCell(new Phrase(label.toUpperCase(AR_LOCALE), LABEL_FONT));
    labelCell.setBorder(0);
    labelCell.setPaddingBottom(4);
    table.addCell(labelCell);

    final PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "—" : value, VALUE_FONT));
    valueCell.setBorder(0);
    valueCell.setPaddingBottom(4);
    table.addCell(valueCell);
  }

  private PdfPCell headerCell(final String text) {
    return headerCell(text, Element.ALIGN_LEFT);
  }

  private PdfPCell headerCell(final String text, final int alignment) {
    final PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
    cell.setBackgroundColor(TABLE_HEADER_BG);
    cell.setHorizontalAlignment(alignment);
    cell.setPadding(6);
    cell.setBorderWidth(0);
    return cell;
  }

  private PdfPCell bodyCell(final String text, final Color background) {
    return bodyCell(text, background, Element.ALIGN_LEFT);
  }

  private PdfPCell bodyCell(final String text, final Color background, final int alignment) {
    final PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_CELL_FONT));
    cell.setBackgroundColor(background);
    cell.setHorizontalAlignment(alignment);
    cell.setPadding(5);
    cell.setBorderWidth(0);
    return cell;
  }

  private String formatCurrency(final BigDecimal amount) {
    if (amount == null) {
      return "—";
    }
    final BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
    return String.format(AR_LOCALE, "$ %,.2f", scaled);
  }

  private String formatDate(final LocalDate date) {
    return date == null ? "—" : DATE_FORMATTER.format(date);
  }

  private String formatDateTime(final LocalDateTime dateTime) {
    return dateTime == null
        ? "—"
        : DATE_TIME_FORMATTER.format(dateTime.atZone(AR_ZONE));
  }

  private String formatAddress(
      final disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity address) {
    final StringBuilder sb = new StringBuilder();
    if (address.getStreetName() != null) {
      sb.append(address.getStreetName());
      if (address.getBlockStreet() != null) {
        sb.append(' ').append(address.getBlockStreet());
      }
    }
    if (address.getApartment() != null && !address.getApartment().isBlank()) {
      sb.append(", dpto ").append(address.getApartment());
    }
    if (address.getFlat() != null && !address.getFlat().isBlank()) {
      sb.append(", piso ").append(address.getFlat());
    }
    if (address.getCity() != null) {
      sb.append(" — ").append(address.getCity().getName());
      if (address.getCity().getProvince() != null) {
        sb.append(", ").append(address.getCity().getProvince().getName());
      }
    }
    return sb.length() == 0 ? "—" : sb.toString();
  }

  private String nullSafe(final String value) {
    return value == null ? "" : value;
  }
}
