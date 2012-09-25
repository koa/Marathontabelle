package ch.bergturbenthal.marathontabelle.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.PhaseData;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;

import com.itextpdf.text.DocumentException;

public class TestMakePdf {
  private static final DateTimeFormatter DURATION_PATTERN = DateTimeFormat.forPattern("mm:ss");
  private static final DateTimeFormatter TIME_PATTERN = DateTimeFormat.forPattern("HH:mm:ss 'Uhr'");

  @Test
  public void generateTestPdf() throws FileNotFoundException, DocumentException {
    final PhaseData phaseA = new PhaseData();
    phaseA.setStartTime(LocalTime.parse("12:24"));
    phaseA.setMaxTime(Duration.standardSeconds(60 * 22 + 17));
    phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseA.setLength(5200);
    phaseA.setVelocity(Double.valueOf(14));
    phaseA.setDefaultPoints();

    final PhaseData phaseD = new PhaseData();
    // phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseD.setMaxTime(Duration.standardSeconds(60 * 9 + 18));
    // phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseD.setLength(930);
    phaseD.setVelocity(Double.valueOf(6));
    phaseD.setDefaultPoints();

    final PhaseData phaseE = new PhaseData();
    // phaseE.setStartTime(LocalTime.parse("11:35"));
    phaseE.setLength(Integer.valueOf(6500));
    phaseE.setMaxTime(Duration.standardSeconds(30 * 60));
    phaseE.setMinTime(phaseE.getMaxTime().minus(Duration.standardMinutes(3)));
    phaseE.setVelocity(Double.valueOf(13));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(1000), "km1"));
    phaseE.getEntries().add(new TimeEntry(null, "H1"));
    phaseE.getEntries().add(new TimeEntry(null, "PO1"));
    phaseE.getEntries().add(new TimeEntry(null, "PO2"));
    phaseE.getEntries().add(new TimeEntry(null, "PO3"));
    phaseE.getEntries().add(new TimeEntry(null, "H2"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(2000), "km2"));
    phaseE.getEntries().add(new TimeEntry(null, "PO4"));
    phaseE.getEntries().add(new TimeEntry(null, "H3"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(3000), "km3"));
    phaseE.getEntries().add(new TimeEntry(null, "PO5"));
    phaseE.getEntries().add(new TimeEntry(null, "PO6"));
    phaseE.getEntries().add(new TimeEntry(null, "H4"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(4000), "km4"));
    phaseE.getEntries().add(new TimeEntry(null, "H5"));
    phaseE.getEntries().add(new TimeEntry(null, "PO7"));
    phaseE.getEntries().add(new TimeEntry(null, "PO8"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(5000), "km5"));
    phaseE.getEntries().add(new TimeEntry(null, "H6"));
    phaseE.getEntries().add(new TimeEntry(null, "H7"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(6000), "km6"));
    phaseE.getEntries().add(new TimeEntry(null, "PO9"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(6500), "Finish E"));

    final FileOutputStream os = new FileOutputStream(new File("target/test.pdf"));

    new GeneratePdf().makePdf(os, phaseA, phaseD, phaseE);
  }
}
