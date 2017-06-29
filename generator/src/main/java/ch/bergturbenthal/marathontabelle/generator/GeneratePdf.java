package ch.bergturbenthal.marathontabelle.generator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import ch.bergturbenthal.marathontabelle.model.DriverData;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCategory;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCompetition;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
  private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm:ss 'Uhr'");
  private File currentDirectory;

  private void appendCell(final PdfPTable table, final String text, final Font font) {
    table.addCell(new Phrase(text, font));
  }

  private void appendImage(final Document document, final PdfWriter writer, final PhaseDataCategory phaseDataCategory,
                           final PhaseDataCompetition phaseData) throws DocumentException {
    if (currentDirectory == null)
      return;
    final String imageName = phaseData.getImageName();
    if (imageName == null)
      return;
    final File imageFile = new File(currentDirectory, imageName);
    if (!imageFile.exists())
      return;
    try {
      final Image image = Image.getInstance(imageFile.getAbsolutePath());
      final Rectangle pageSize = document.getPageSize();
      final float imageWidth = pageSize.getWidth() - pageSize.getBorderWidthLeft() - pageSize.getBorderWidthRight();
      final float originalImageWidth = image.getWidth();
      final float verticalPosition = writer.getVerticalPosition(true);
      final float height = pageSize.getHeight();
      final float borderWidthBottom = pageSize.getBorderWidthBottom();
      final float remainingHeight = verticalPosition + borderWidthBottom;

      image.setAlignment(Image.ALIGN_CENTER);
      image.scaleToFit(imageWidth * 0.9f, remainingHeight * 0.9f);
      document.add(image);
      document.newPage();
    } catch (BadElementException | IOException e) {
      log.warn("Cannot layout image " + imageName, e);
    }
  }

  private void appendPhaseOverview(final Document document, final PdfWriter writer, final MarathonData marathon, final Phase phase,
                                   final String driver) throws DocumentException {
    final PhaseDataCompetition phaseData = marathon.getCompetitionPhases().get(phase);
    if (phaseData.getLength() == null)
      return;
    final DriverData driverData = marathon.getDrivers().get(driver);
    final PhaseDataCategory phaseDataCategory = phaseData.getCategoryTimes().get(driverData.getCategory());
    appendTitle(document, phaseData.getPhaseName());
    apppendTimeOverview(document, marathon, phase, driver);
    appendTimeDetail(document, phaseDataCategory, phaseData);
    appendTimetable(document, phaseDataCategory, phaseData);
    appendImage(document, writer, phaseDataCategory, phaseData);
  }

  /**
   * @param document
   * @param phase
   * @throws DocumentException
   */
  private void appendSmallSheets(final Document document, final MarathonData marathonData, final String driver) throws DocumentException {
    final DriverData driverData = marathonData.getDrivers().get(driver);
    if (driverData.getSmallSheets().isEmpty())
      return;
    document.newPage();
    final PdfPTable table = new PdfPTable(3);
    table.setWidthPercentage(100);
    // table.getDefaultCell().setBorder(0);
    int cellNr = 0;
    for (final Phase phaseId : Phase.values()) {
      final PhaseDataCompetition phase = marathonData.getCompetitionPhases().get(phaseId);
      final String phaseName = phase.getPhaseName();
      if (!driverData.getSmallSheets().contains(phaseId))
        continue;
      table.addCell(makeEntryTable(++cellNr, phaseName, null, null, driver));

      final Collection<TimeEntry> entries = phase.getEntries();
      if (entries == null || entries.size() == 0)
        continue;

      final PhaseDataCategory phaseDataCategory = phase.getCategoryTimes().get(driverData.getCategory());

      final Duration minTime = phaseDataCategory.getMinTime();
      final Duration maxTime = phaseDataCategory.getMaxTime();
      final Integer length = phase.getLength();
      final Double minMillisPerMeter = minTime != null ? Double.valueOf(minTime.toMillis() / (double) length.intValue()) : null;
      final Double maxMillisPerMeter = maxTime != null ? Double.valueOf(maxTime.toMillis() / (double) length.intValue()) : null;

      for (final TimeEntry entry : entries) {
        final String comment = entry.getComment();
        if (comment == null)
          continue;

        // entryTable.getDefaultCell().setMinimumHeight(document.getPageSize().getHeight()
        // / 3);
        String[] lineTitles = null;
        String[] lineValues = null;
        final Integer position = entry.getPosition();
        if (position != null) {
          lineTitles = new String[3];
          lineValues = new String[3];
          lineTitles[0] = "Strecke";
          lineValues[0] = position + " m";
          if (minMillisPerMeter != null) {
            lineTitles[1] = "min. ";
            lineValues[1] = FormatUtil.formatDuration(Duration.ofMillis((long) (position.intValue() * minMillisPerMeter.doubleValue())));
          }
          if (maxMillisPerMeter != null) {
            lineTitles[2] = "max. ";
            lineValues[2] = FormatUtil.formatDuration(Duration.ofMillis((long) (position.intValue() * maxMillisPerMeter.doubleValue())));
          }
        }
        table.addCell(makeEntryTable(++cellNr, comment, lineTitles, lineValues, driver));
      }
    }
    table.completeRow();
    if (cellNr > 0)
      document.add(table);

  }

  private void appendTimeDetail(final Document document, final PhaseDataCategory category,
                                final PhaseDataCompetition phase) throws DocumentException {
    final PdfPTable table = new PdfPTable(5);
    // table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    final Integer length = phase.getLength();
    final Duration minTime = category.getMinTime();
    final Duration maxTime = category.getMaxTime();
    final Double velocity = category.getVelocity();
    if (minTime != null || maxTime != null) {
      table.getDefaultCell().setBorder(Rectangle.TOP + Rectangle.LEFT);
      table.addCell("Dauer");
      table.getDefaultCell().setBorder(Rectangle.TOP + Rectangle.RIGHT);
      table.addCell("");
    }
    if (minTime != null) {
      table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
      table.completeRow();
      table.getDefaultCell().setBorder(Rectangle.LEFT);
      table.addCell("min.");
      table.getDefaultCell().setBorder(Rectangle.RIGHT);
      table.addCell(FormatUtil.formatDuration(minTime));
    }
    table.getDefaultCell().setBorder(Rectangle.BOTTOM + Rectangle.TOP);
    if (length != null) {
      table.addCell("Strecke");
      table.addCell(Integer.toString(length) + " m");
    } else {
      table.addCell("");
      table.addCell("");
    }
    final boolean canComputeTime = length != null && velocity != null;
    if (canComputeTime) {
      table.getDefaultCell().setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.TOP);
      table.addCell("Theor. Zeit");
    }
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    table.completeRow();

    table.getDefaultCell().setBorder(Rectangle.LEFT + Rectangle.BOTTOM);
    if (maxTime != null) {
      table.addCell("max.");
      table.getDefaultCell().setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
      table.addCell(FormatUtil.formatDuration(maxTime));
    } else {
      table.addCell("");
      table.getDefaultCell().setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
      table.addCell("");
    }
    table.getDefaultCell().setBorder(Rectangle.BOTTOM + Rectangle.TOP);
    if (velocity != null) {
      table.addCell("Geschw.");
      table.addCell(velocity + " km/h");
    } else {
      table.addCell("");
      table.addCell("");
    }
    if (canComputeTime) {
      table.getDefaultCell().setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
      final double theoritcallyDurationInMillis = length / velocity / 1000 * 3600 * 1000;
      table.addCell(FormatUtil.formatDuration(Duration.ofMillis(((long) theoritcallyDurationInMillis))));
    }
    table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    table.completeRow();

    table.setWidthPercentage(100);
    table.setSpacingAfter(20);

    document.add(table);

  }

  private void appendTimetable(final Document document, final PhaseDataCategory category, final PhaseDataCompetition phase) throws DocumentException {
    final List<TimeEntry> entries = phase.getEntries();
    if (entries == null)
      return;
    final Integer length = phase.getLength();
    final Duration minTime = category.getMinTime();
    final Duration maxTime = category.getMaxTime();
    if (length == null)
      return;
    final Double minMillisPerMeter = minTime != null ? Double.valueOf(minTime.toMillis() / (double) length.intValue()) : null;
    final Double maxMillisPerMeter = maxTime != null ? Double.valueOf(maxTime.toMillis() / (double) length.intValue()) : null;
    final PdfPTable table = new PdfPTable(4);

    table.setHeaderRows(1);
    table.addCell("Strecke");
    table.addCell("min.");
    table.addCell("max.");
    table.addCell("Bemerkungen");
    table.completeRow();
    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
    final Font font = new Font(Font.FontFamily.HELVETICA, 32);
    final StringBuilder commentBuilder = new StringBuilder();
    for (final TimeEntry entry : entries) {
      if (entry.isOnlySmallSheet())
        continue;

      if (entry.getComment() != null && !entry.getComment().isEmpty()) {
        if (commentBuilder.length() > 0)
          commentBuilder.append(", ");
        commentBuilder.append(entry.getComment());
      }
      if (entry.getPosition() == null)
        continue;
      appendCell(table, entry.getPosition() + " m", font);
      if (minMillisPerMeter != null) {
        appendCell(table, FormatUtil.formatDuration(Duration.ofMillis((long) (entry.getPosition().intValue() * minMillisPerMeter.doubleValue()))),
                   font);
      } else
        table.addCell("");
      if (maxMillisPerMeter != null) {
        appendCell(table, FormatUtil.formatDuration(Duration.ofMillis((long) (entry.getPosition().intValue() * maxMillisPerMeter.doubleValue()))),
                   font);
      } else
        table.addCell("");
      if (commentBuilder.length() > 0) {
        table.addCell(commentBuilder.toString());
        commentBuilder.setLength(0);
      }
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

  private void apppendTimeOverview(final Document document, final MarathonData marathon, final Phase phase,
                                   final String driver) throws DocumentException {
    final DriverData driverData = marathon.getDrivers().get(driver);
    final LocalTime startTime = driverData.getStartTimes().get(phase);
    final PhaseDataCompetition phaseData = marathon.getCompetitionPhases().get(phase);
    final PhaseDataCategory phaseDataCategory = phaseData.getCategoryTimes().get(driverData.getCategory());
    final Duration minTime = phaseDataCategory.getMinTime();
    final Duration maxTime = phaseDataCategory.getMaxTime();
    if (startTime != null) {
      final PdfPTable table = new PdfPTable(2);
      table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
      table.getDefaultCell().setBackgroundColor(BaseColor.YELLOW);
      table.addCell("Startzeit");
      table.addCell(TIME_PATTERN.format(startTime));
      if (minTime != null) {
        table.addCell("Ankunft Min");
        table.addCell(TIME_PATTERN.format(startTime.plus(minTime)));
      }
      if (maxTime != null) {
        table.addCell("Ankunft Max");
        table.addCell(TIME_PATTERN.format(startTime.plus(maxTime)));
      }
      table.setWidthPercentage(30);
      table.setSpacingAfter(5);
      document.add(table);
    }
  }

  private PdfPTable makeEntryTable(final int nr, final String comment, final String[] lineTitles, final String[] lineValues,
                                   final String driverName) {
    final PdfPTable entryTable = new PdfPTable(new float[] { 1, 2 });
    entryTable.getDefaultCell().setBorder(0);
    {
      final PdfPCell nrCell = new PdfPCell();
      final String headString = Integer.toString(nr) + " - " + driverName;
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

  public void makePdf(final OutputStream out, final MarathonData data, final String driver) {
    try {
      final DriverData driverData = data.getDrivers().get(driver);
      if (driverData != null && driverData.getCategory() != null) {
        final String now = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(Instant.now().atZone(ZoneId.systemDefault()));
        final Document document = new Document(PageSize.A4);
        final PdfWriter writer = PdfWriter.getInstance(document, out);
        final String category = driverData.getCategory();
        writer.setPageEvent(new PdfPageEventHelper() {

          @Override
          public void onEndPage(final PdfWriter writer, final Document document) {
            final PdfContentByte cb = writer.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(driver + " - " + category + " - " + now, SMALL_FONT),
                                       document.leftMargin() - 1, document.top() + 30, 0);
          }
        });

        document.open();
        appendPhaseOverview(document, writer, data, Phase.A, driver);
        appendPhaseOverview(document, writer, data, Phase.TRANSFER, driver);
        document.newPage();
        appendPhaseOverview(document, writer, data, Phase.B, driver);
        appendSmallSheets(document, data, driver);
        document.close();
      }
    } catch (final DocumentException e) {
      throw new RuntimeException(e);
    }

  }

  public GeneratePdf withCurrentDirectory(final File directory) {
    this.currentDirectory = directory;
    return this;
  }

}
