/*
 * (c) 2012 panter llc, Zurich, Switzerland.
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
public class Bern {

  @Test
  public void makeAndrea() throws DocumentException, IOException {
    final MarathonData data = makeData();
    data.getPhaseA().setStartTime(LocalTime.parse("09:03"));
    data.getPhaseD().setStartTime(LocalTime.parse("09:24"));
    data.getPhaseE().setStartTime(LocalTime.parse("09:57"));

    final FileOutputStream os = new FileOutputStream(new File("target/andrea-bern.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, true, false, true);
    } finally {
      os.close();
    }
  }

  @Test
  public void makeChristof() throws DocumentException, IOException {
    final MarathonData data = makeData();
    data.getPhaseA().setStartTime(LocalTime.parse("08:45"));
    data.getPhaseD().setStartTime(LocalTime.parse("09:06"));
    data.getPhaseE().setStartTime(LocalTime.parse("09:39"));

    final FileOutputStream os = new FileOutputStream(new File("target/christof-bern.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, true, false, true);
    } finally {
      os.close();
    }
  }

  private MarathonData makeData() {
    final PhaseData phaseA = new PhaseData();
    phaseA.setMaxTime(Duration.standardSeconds(60 * 17 + 8));
    phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseA.setLength(4000);
    phaseA.setVelocity(Double.valueOf(14));
    phaseA.setDefaultPoints();

    final PhaseData phaseD = new PhaseData();
    // phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseD.setMaxTime(Duration.standardSeconds(60 * 8 + 9));
    phaseD.setLength(816);
    phaseD.setVelocity(Double.valueOf(6));
    phaseD.setDefaultPoints();

    final PhaseData phaseE = new PhaseData();
    // phaseE.setStartTime(LocalTime.parse("11:35"));
    phaseE.setLength(Integer.valueOf(4400));
    phaseE.setMaxTime(Duration.standardSeconds(30 * 20 + 18));
    phaseE.setMinTime(phaseE.getMaxTime().minus(Duration.standardMinutes(3)));
    phaseE.setVelocity(Double.valueOf(13));
    phaseE.getEntries().add(new TimeEntry(null, "H1"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(1000), "km1"));
    phaseE.getEntries().add(new TimeEntry(null, "H2"));
    phaseE.getEntries().add(new TimeEntry(null, "H3"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(2000), "km2"));
    phaseE.getEntries().add(new TimeEntry(null, "H4"));
    phaseE.getEntries().add(new TimeEntry(null, "H5"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(3000), "km3"));
    phaseE.getEntries().add(new TimeEntry(null, "H6"));
    phaseE.getEntries().add(new TimeEntry(null, "H7"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(4000), "km4"));
    phaseE.getEntries().add(new TimeEntry(Integer.valueOf(4400), "Finish E"));
    final MarathonData data = new MarathonData(phaseA, phaseD, phaseE);
    return data;
  }

}
