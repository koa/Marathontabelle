/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.Test;

import com.itextpdf.text.DocumentException;

import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.DriverData;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCategory;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCompetition;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;

/**
 * TODO: add type comment.
 *
 */
public class TestAltenfelden {
  @Test
  public void makeChristof() throws DocumentException, IOException {
    final MarathonData data = makeData();
    final DriverData driverData = new DriverData();
    data.getDrivers().put("Christof König", driverData);
    driverData.getStartTimes().put(Phase.A, LocalTime.parse("08:45"));
    driverData.getStartTimes().put(Phase.TRANSFER, LocalTime.parse("09:06"));
    driverData.getStartTimes().put(Phase.B, LocalTime.parse("09:39"));
    driverData.getSmallSheets().addAll(Arrays.asList(Phase.values()));
    driverData.setCategory("Pony 2 Spänner");

    final FileOutputStream os = new FileOutputStream(new File("target/christof-altenfelden.pdf"));
    try {

      final File directory = new File("src/test/resources/images");
      new GeneratePdf().withCurrentDirectory(directory).makePdf(os, data, "Christof König");
    } finally {
      os.close();
    }
  }

  private MarathonData makeData() {
    final MarathonData data = new MarathonData();
    final String category = "Pony 2 Spänner";
    data.getCategories().add(category);
    final PhaseDataCompetition phaseA = new PhaseDataCompetition();
    data.getCompetitionPhases().put(Phase.A, phaseA);
    final PhaseDataCategory phaseADataCategory = new PhaseDataCategory();
    phaseA.getCategoryTimes().put(category, phaseADataCategory);
    phaseADataCategory.setMaxTime(Duration.ofSeconds(60 * 26 + 59));
    phaseADataCategory.setMinTime(phaseADataCategory.getMaxTime().minus(Duration.ofMinutes(2)));
    phaseA.setPhaseName("Phase A");
    phaseA.setLength(6300);
    phaseADataCategory.setVelocity(Double.valueOf(14));
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
    phaseA.setImageName("IMG_20170629_113617.jpg");

    final PhaseDataCompetition phaseD = new PhaseDataCompetition();
    data.getCompetitionPhases().put(Phase.TRANSFER, phaseD);
    final PhaseDataCategory phaseDDataCategory = new PhaseDataCategory();
    phaseD.getCategoryTimes().put(category, phaseDDataCategory);
    // phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseDDataCategory.setMaxTime(Duration.ofSeconds(60 * 8 + 24));
    phaseD.setPhaseName("Transfer");
    phaseD.setLength(700);
    phaseDDataCategory.setVelocity(Double.valueOf(5));
    phaseD.getEntries().add(new TimeEntry(null, "T1"));

    final PhaseDataCompetition phaseE = new PhaseDataCompetition();
    data.getCompetitionPhases().put(Phase.B, phaseE);
    final PhaseDataCategory phaseEDataCategory = new PhaseDataCategory();
    phaseE.getCategoryTimes().put(category, phaseEDataCategory);
    phaseE.setPhaseName("Phase E");
    phaseE.setLength(Integer.valueOf(9000));
    phaseEDataCategory.setMaxTime(Duration.ofSeconds(60 * 41 + 32));
    phaseEDataCategory.setMinTime(phaseEDataCategory.getMaxTime().minus(Duration.ofMinutes(3)));
    phaseEDataCategory.setVelocity(Double.valueOf(13));
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

    return data;
  }

}
