package ch.bergturbenthal.marathontabelle.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

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
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class TestMakePdf {
  private static final DateTimeFormatter DURATION_PATTERN = DateTimeFormat.forPattern("mm:ss");
  private static final DateTimeFormatter TIME_PATTERN = DateTimeFormat.forPattern("HH:mm:ss 'Uhr'");

  @Test
  public void generateTestPdf() throws FileNotFoundException, DocumentException {
    final PhaseData phaseA = new PhaseData();
    phaseA.setStartTime(LocalTime.parse("10:35"));
    phaseA.setMaxTime(Duration.standardSeconds(60 * 37 + 51));
    phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseA.setLength(8200);
    phaseA.setVelocity(Double.valueOf(13));
    phaseA.setDefaultPoints();

    final PhaseData phaseD = new PhaseData();
    phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseD.setMaxTime(Duration.standardSeconds(60 * 7 + 14));
    // phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseD.setLength(725);
    phaseD.setVelocity(Double.valueOf(6));
    phaseD.setDefaultPoints();

    final Document document = new Document(PageSize.A4);
    PdfWriter.getInstance(document, new FileOutputStream(new File("target/test.pdf")));
    document.open();
    appendTitle(document, "Phase A");
    apppendTimeOverview(document, phaseA);
    appendTimeDetail(document, phaseA);
    appendTimetable(document, phaseA);

    appendTitle(document, "Phase D");
    apppendTimeOverview(document, phaseD);
    appendTimeDetail(document, phaseD);
    appendTimetable(document, phaseD);

    document.close();

  }

  private void appendCell(final PdfPTable table, final String text, final Font font) {
    table.addCell(new Phrase(text, font));
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
        appendCell(table, DURATION_PATTERN.print((long) (entry.getPosition().intValue() * minMillisPerMeter)), font);
      } else
        table.addCell("");
      if (maxMillisPerMeter != null) {
        appendCell(table, DURATION_PATTERN.print((long) (entry.getPosition().intValue() * maxMillisPerMeter)), font);
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
