/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.junit.Test;

import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.PhaseData;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;

import com.itextpdf.text.DocumentException;

/**
 * TODO: add type comment.
 * 
 */
public class Altenfelden {
  @Test
  public void makeChristof() throws DocumentException, IOException {
    final MarathonData data = makeData();
    data.getPhaseA().setStartTime(LocalTime.parse("08:45"));
    data.getPhaseD().setStartTime(LocalTime.parse("09:06"));
    data.getPhaseE().setStartTime(LocalTime.parse("09:39"));

    final FileOutputStream os = new FileOutputStream(new File("target/christof-altenfelden.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, true, true, true);
    } finally {
      os.close();
    }
  }

  private MarathonData makeData() {
    final PhaseData phaseA = new PhaseData();
    phaseA.setMaxTime(Duration.standardSeconds(60 * 26 + 59));
    phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseA.setLength(6300);
    phaseA.setVelocity(Double.valueOf(14));
    phaseA.getEntries().add(new TimeEntry(null, "T1"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(1000), "km1"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(2000), "T2"));
    phaseA.getEntries().add(new TimeEntry(null, "T3"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(3000), "km3"));
    phaseA.getEntries().add(new TimeEntry(null, "T4"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(4000), "km4"));
    phaseA.getEntries().add(new TimeEntry(null, "T5"));
    phaseA.getEntries().add(new TimeEntry(null, "T6"));
    phaseA.getEntries().add(new TimeEntry(null, "T7"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(5000), "km5"));
    phaseA.getEntries().add(new TimeEntry(null, "T8"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(6000), "km6"));
    phaseA.getEntries().add(new TimeEntry(Integer.valueOf(6300), "Ziel"));

    final PhaseData phaseD = new PhaseData();
    // phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseD.setMaxTime(Duration.standardSeconds(60 * 8 + 24));
    phaseD.setLength(700);
    phaseD.setVelocity(Double.valueOf(5));
    phaseD.getEntries().add(new TimeEntry(null, "T1"));

    final PhaseData phaseE = new PhaseData();
    // phaseE.setStartTime(LocalTime.parse("11:35"));
    phaseE.setLength(Integer.valueOf(9000));
    phaseE.setMaxTime(Duration.standardSeconds(60 * 41 + 32));
    phaseE.setMinTime(phaseE.getMaxTime().minus(Duration.standardMinutes(3)));
    phaseE.setVelocity(Double.valueOf(13));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(800, "H1"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(1000), "km1"));
    phaseE.getEntries().add(new TimeEntry(null, "T1"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(2000), "km2 / H2"));
    phaseE.getEntries().add(new TimeEntry(null, "T2"));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(2650, "H3"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(3000), "km3 / T3"));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(3350, "H4"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(4000), "km4"));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(3800, "H5"));
    phaseE.getEntries().add(new TimeEntry(null, "T4"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(5000), "km5"));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(5450, "H6"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(6000), "km6"));
    phaseE.getEntries().add(new TimeEntry(null, "T5"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(7000), "km7"));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(7150, "H7"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(8000), "km8 / T6"));
    phaseE.getEntries().add(TimeEntry.makePositionedEntrySmallSheetOnly(8450, "H8"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(8700), "-300m"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(9000), "Finish E"));

    final MarathonData data = new MarathonData(phaseA, phaseD, phaseE);
    return data;
  }

}
