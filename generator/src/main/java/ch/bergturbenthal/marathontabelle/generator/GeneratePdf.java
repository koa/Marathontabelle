package ch.bergturbenthal.marathontabelle.generator;

import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
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
  /**
   * 
   */
  private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 40);
  /**
   * 
   */
  private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8);
  /**
   * 
   */
  private static final Font DATA_FONT = new Font(Font.FontFamily.HELVETICA, 24);
  private static final DateTimeFormatter DURATION_PATTERN = DateTimeFormat.forPattern("mm:ss");
  private static final DateTimeFormatter TIME_PATTERN = DateTimeFormat.forPattern("HH:mm:ss 'Uhr'");

  public void makePdf(final OutputStream out, final MarathonData data, final boolean makeSmallSheetsA, final boolean makeSmallSheetsD,
                      final boolean makeSmallSheetsE) {
    try {
      final Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, out);
      document.open();
      appendPhaseOverview(document, data.getPhaseA(), "Phase A");
      appendPhaseOverview(document, data.getPhaseD(), "Phase D");
      document.newPage();
      appendPhaseOverview(document, data.getPhaseE(), "Phase E");
      final Map<Phase, PhaseData> smallSheetPhase = new LinkedHashMap<Phase, PhaseData>();
      if (makeSmallSheetsA) {
        smallSheetPhase.put(Phase.A, data.getPhaseA());
      }
      if (makeSmallSheetsD) {
        smallSheetPhase.put(Phase.D, data.getPhaseD());
      }
      if (makeSmallSheetsE) {
        smallSheetPhase.put(Phase.E, data.getPhaseE());
      }
      if (smallSheetPhase.size() > 0) {
        document.newPage();
        appendSmallSheets(document, smallSheetPhase);
      }
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
  private void appendSmallSheets(final Document document, final Map<Phase, PhaseData> phases) throws DocumentException {
    final PdfPTable table = new PdfPTable(3);
    table.setWidthPercentage(100);
    // table.getDefaultCell().setBorder(0);
    int cellNr = 0;
    for (final Entry<Phase, PhaseData> phaseEntry : phases.entrySet()) {
      final PhaseData phase = phaseEntry.getValue();
      if (phases.size() > 0) {
        String phaseName = null;
        switch (phaseEntry.getKey()) {
        case A:
          phaseName = "Phase A";
          break;
        case D:
          phaseName = "Phase D";
          break;
        case E:
          phaseName = "Phase E";
          break;
        }
        table.addCell(makeEntryTable(++cellNr, phaseName, null, null));
      }

      final Collection<TimeEntry> entries = phase.getEntries();
      if (entries == null || entries.size() == 0)
        continue;
      final Double minMillisPerMeter =
                                       phase.getMinTime() != null ? Double.valueOf(phase.getMinTime().getMillis()
                                                                                   / (double) phase.getLength().intValue()) : null;
      final Double maxMillisPerMeter =
                                       phase.getMaxTime() != null ? Double.valueOf(phase.getMaxTime().getMillis()
                                                                                   / (double) phase.getLength().intValue()) : null;

      for (final TimeEntry entry : entries) {
        final String comment = entry.getComment();
        if (comment == null)
          continue;

        // entryTable.getDefaultCell().setMinimumHeight(document.getPageSize().getHeight()
        // / 3);
        String[] lineTitles = null;
        String[] lineValues = null;
        if (entry.getPosition() != null) {
          lineTitles = new String[3];
          lineValues = new String[3];
          lineTitles[0] = "Strecke";
          lineValues[0] = entry.getPosition() + " m";
          if (minMillisPerMeter != null) {
            lineTitles[1] = "min. ";
            lineValues[1] = DURATION_PATTERN.print((long) (entry.getPosition().intValue() * minMillisPerMeter.doubleValue()));
          }
          if (maxMillisPerMeter != null) {
            lineTitles[2] = "max. ";
            lineValues[2] = DURATION_PATTERN.print((long) (entry.getPosition().intValue() * maxMillisPerMeter.doubleValue()));
          }
        }
        table.addCell(makeEntryTable(++cellNr, comment, lineTitles, lineValues));
      }
    }
    table.completeRow();
    if (cellNr > 0)
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

  private PdfPTable makeEntryTable(final int nr, final String comment, final String[] lineTitles, final String[] lineValues) {
    final PdfPTable entryTable = new PdfPTable(new float[] { 1, 2 });
    entryTable.getDefaultCell().setBorder(0);
    {
      final PdfPCell nrCell = new PdfPCell();
      final String headString = Integer.toString(nr);
      nrCell.setColspan(2);
      nrCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      nrCell.setPhrase(new Phrase(headString, SMALL_FONT));
      nrCell.setMinimumHeight(50);
      nrCell.setBorder(0);
      entryTable.addCell(nrCell);
    }
    {
      final PdfPCell commentCell = new PdfPCell();
      commentCell.setColspan(2);
      commentCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
      commentCell.setPhrase(new Phrase(comment, TITLE_FONT));
      commentCell.setMinimumHeight(80);
      commentCell.setBorder(0);
      entryTable.addCell(commentCell);
    }
    if (lineTitles != null && lineValues != null) {
      entryTable.getDefaultCell().setMinimumHeight(40);
      entryTable.getDefaultCell().setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
      entryTable.getDefaultCell().setBorder(PdfPCell.BOTTOM);

      for (int i = 0; i < 3; i++) {
        if (lineTitles[i] != null || lineValues[i] != null) {
          entryTable.addCell(lineTitles[i]);
          appendCell(entryTable, lineValues[i], DATA_FONT);
        } else {
          entryTable.addCell("");
          entryTable.completeRow();
        }
      }

    } else {
      entryTable.getDefaultCell().setMinimumHeight(120);
      entryTable.addCell("");
      entryTable.completeRow();
    }
    return entryTable;
  }

}
