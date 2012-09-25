package ch.bergturbenthal.marathontabelle.generator;

import java.io.OutputStream;
import java.util.Collection;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.bergturbenthal.marathontabelle.model.PhaseData;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class GeneratePdf {
  private static final DateTimeFormatter DURATION_PATTERN = DateTimeFormat.forPattern("mm:ss");
  private static final DateTimeFormatter TIME_PATTERN = DateTimeFormat.forPattern("HH:mm:ss 'Uhr'");

  public void makePdf(final OutputStream out, final PhaseData phaseA, final PhaseData phaseD, final PhaseData phaseE) {
    try {
      final Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, out);
      document.open();
      appendPhaseOverview(document, phaseA, "Phase A");
      appendPhaseOverview(document, phaseD, "Phase D");
      document.newPage();
      appendPhaseOverview(document, phaseE, "Phase E");
      document.newPage();
      appendSmallSheets(document, phaseE);
      document.close();
    } catch (final DocumentException e) {
      throw new RuntimeException(e);
    }

  }

  private void appendCell(final PdfPTable table, final String text, final Font font) {
    table.addCell(new Phrase(text, font));
  }

  private void appendPhaseOverview(final Document document, final PhaseData phaseData, final String title) throws DocumentException {
    appendTitle(document, title);
    apppendTimeOverview(document, phaseData);
    appendTimeDetail(document, phaseData);
    appendTimetable(document, phaseData);
  }

  /**
   * @param document
   * @param phase
   * @throws DocumentException
   */
  private void appendSmallSheets(final Document document, final PhaseData phase) throws DocumentException {
    final Collection<TimeEntry> entries = phase.getEntries();
    if (entries == null || entries.size() == 0)
      return;
    final Double minMillisPerMeter =
                                     phase.getMinTime() != null ? Double.valueOf(phase.getMinTime().getMillis()
                                                                                 / (double) phase.getLength().intValue()) : null;
    final Double maxMillisPerMeter =
                                     phase.getMaxTime() != null ? Double.valueOf(phase.getMaxTime().getMillis()
                                                                                 / (double) phase.getLength().intValue()) : null;

    final Font dataFont = new Font(Font.FontFamily.HELVETICA, 24);
    final Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);
    final Font titleFont = new Font(Font.FontFamily.HELVETICA, 40);

    final PdfPTable table = new PdfPTable(3);
    table.setWidthPercentage(100);
    // table.getDefaultCell().setBorder(0);
    int cellNr = 0;
    for (final TimeEntry entry : entries) {
      final String comment = entry.getComment();
      if (comment == null)
        continue;

      final PdfPTable entryTable = new PdfPTable(new float[] { 1, 2 });
      entryTable.getDefaultCell().setBorder(0);
      // entryTable.getDefaultCell().setMinimumHeight(document.getPageSize().getHeight()
      // / 3);
      final PdfPCell nrCell = new PdfPCell();
      nrCell.setColspan(2);
      nrCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nrCell.setPhrase(new Phrase(Integer.toString(++cellNr), smallFont));
      nrCell.setMinimumHeight(50);
      nrCell.setBorder(0);
      entryTable.addCell(nrCell);

      final PdfPCell commentCell = new PdfPCell();
      commentCell.setColspan(2);
      commentCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      commentCell.setPhrase(new Phrase(comment, titleFont));
      commentCell.setMinimumHeight(80);
      commentCell.setBorder(0);
      entryTable.addCell(commentCell);

      if (entry.getPosition() != null) {
        entryTable.getDefaultCell().setMinimumHeight(40);
        entryTable.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
        entryTable.getDefaultCell().setBorder(PdfPCell.BOTTOM);
        entryTable.addCell("Strecke");
        appendCell(entryTable, entry.getPosition() + " m", dataFont);
        if (minMillisPerMeter != null) {
          entryTable.addCell("min. ");
          appendCell(entryTable, DURATION_PATTERN.print((long) (entry.getPosition().intValue() * minMillisPerMeter.doubleValue())), dataFont);
        } else {
          entryTable.addCell("");
          entryTable.completeRow();
        }
        if (maxMillisPerMeter != null) {
          entryTable.addCell("max. ");
          appendCell(entryTable, DURATION_PATTERN.print((long) (entry.getPosition().intValue() * maxMillisPerMeter.doubleValue())), dataFont);
        } else {
          entryTable.addCell("");
          entryTable.completeRow();
        }
      } else {
        entryTable.getDefaultCell().setMinimumHeight(120);
        entryTable.addCell("");
        entryTable.completeRow();
      }
      table.addCell(entryTable);
    }
    table.completeRow();
    document.add(table);

  }

  private void appendTimeDetail(final Document document, final PhaseData phase) throws DocumentException {
    final PdfPTable table = new PdfPTable(5);
    // table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    if (phase.getMinTime() != null || phase.getMaxTime() != null) {
      table.getDefaultCell().setBorder(Rectangle.TOP + Rectangle.LEFT);
      table.addCell("Dauer");
      table.getDefaultCell().setBorder(Rectangle.TOP + Rectangle.RIGHT);
      table.addCell("");
    }
    if (phase.getMinTime() != null) {
      table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
      table.completeRow();
      table.getDefaultCell().setBorder(Rectangle.LEFT);
      table.addCell("min.");
      table.getDefaultCell().setBorder(Rectangle.RIGHT);
      table.addCell(DURATION_PATTERN.print(phase.getMinTime().getMillis()));
    }
    table.getDefaultCell().setBorder(Rectangle.BOTTOM + Rectangle.TOP);
    if (phase.getLength() != null) {
      table.addCell("Strecke");
      table.addCell(Integer.toString(phase.getLength()) + " m");
    } else {
      table.addCell("");
      table.addCell("");
    }
    final boolean canComputeTime = phase.getLength() != null && phase.getVelocity() != null;
    if (canComputeTime) {
      table.getDefaultCell().setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.TOP);
      table.addCell("Theor. Zeit");
    }
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    table.completeRow();

    table.getDefaultCell().setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
    if (phase.getMaxTime() != null) {
      table.addCell("max.");
      table.getDefaultCell().setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
      table.addCell(DURATION_PATTERN.print(phase.getMaxTime().getMillis()));
    } else {
      table.addCell("");
      table.getDefaultCell().setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
      table.addCell("");
    }
    table.getDefaultCell().setBorder(Rectangle.BOTTOM + Rectangle.TOP);
    if (phase.getVelocity() != null) {
      table.addCell("Geschw.");
      table.addCell(phase.getVelocity() + " km/h");
    } else {
      table.addCell("");
      table.addCell("");
    }
    if (canComputeTime) {
      table.getDefaultCell().setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
      final double theoritcallyDurationInMillis = phase.getLength() / phase.getVelocity() / 1000 * 3600 * 1000;
      table.addCell(DURATION_PATTERN.print((long) theoritcallyDurationInMillis));
    }
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    table.completeRow();

    table.setWidthPercentage(100);
    table.setSpacingAfter(20);

    document.add(table);

  }

  private void appendTimetable(final Document document, final PhaseData phase) throws DocumentException {
    if (phase.getEntries() == null)
      return;
    if (phase.getLength() == null)
      return;
    final Double minMillisPerMeter =
                                     phase.getMinTime() != null ? Double.valueOf(phase.getMinTime().getMillis()
                                                                                 / (double) phase.getLength().intValue()) : null;
    final Double maxMillisPerMeter =
                                     phase.getMaxTime() != null ? Double.valueOf(phase.getMaxTime().getMillis()
                                                                                 / (double) phase.getLength().intValue()) : null;
    final PdfPTable table = new PdfPTable(4);

    table.setHeaderRows(1);
    table.addCell("Strecke");
    table.addCell("min.");
    table.addCell("max.");
    table.addCell("Bemerkungen");
    table.completeRow();
    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
    final Font font = new Font(Font.FontFamily.HELVETICA, 32);
    for (final TimeEntry entry : phase.getEntries()) {
      if (entry.getPosition() == null)
        continue;
      appendCell(table, entry.getPosition() + " m", font);
      if (minMillisPerMeter != null) {
        appendCell(table, DURATION_PATTERN.print((long) (entry.getPosition().intValue() * minMillisPerMeter.doubleValue())), font);
      } else
        table.addCell("");
      if (maxMillisPerMeter != null) {
        appendCell(table, DURATION_PATTERN.print((long) (entry.getPosition().intValue() * maxMillisPerMeter.doubleValue())), font);
      }
      if (entry.getComment() != null)
        table.addCell(entry.getComment());
      table.completeRow();
    }
    if (table.getRows().size() < 2)
      return;

    table.setWidthPercentage(100);
    table.setSpacingAfter(20);

    document.add(table);
  }

  private void appendTitle(final Document document, final String title) throws DocumentException {
    final Paragraph titleParagraph = new Paragraph(title);
    titleParagraph.setAlignment(Element.ALIGN_CENTER);
    titleParagraph.setSpacingAfter(20);
    document.add(titleParagraph);
  }

  private void apppendTimeOverview(final Document document, final PhaseData phase) throws DocumentException {
    if (phase.getStartTime() != null) {
      final PdfPTable table = new PdfPTable(2);
      table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
      table.getDefaultCell().setBackgroundColor(BaseColor.YELLOW);
      table.addCell("Startzeit");
      table.addCell(TIME_PATTERN.print(phase.getStartTime()));
      if (phase.getMinTime() != null) {
        table.addCell("Ankunft Min");
        table.addCell(TIME_PATTERN.print(phase.getStartTime().plusMillis((int) phase.getMinTime().getMillis())));
      }
      if (phase.getMaxTime() != null) {
        table.addCell("Ankunft Max");
        table.addCell(TIME_PATTERN.print(phase.getStartTime().plusMillis((int) phase.getMaxTime().getMillis())));
      }
      table.setWidthPercentage(30);
      table.setSpacingAfter(5);
      document.add(table);
    }
  }

}
