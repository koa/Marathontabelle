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

import com.itextpdf.text.DocumentException;

/**
 * TODO: add type comment.
 * 
 */
public class Frauenfeld {

  @Test
  public void makeAndrea() throws DocumentException, IOException {
    final MarathonData data = makePonyData();
    data.getPhaseA().setStartTime(LocalTime.parse("10:32"));
    data.getPhaseD().setStartTime(LocalTime.parse("10:57"));
    data.getPhaseE().setStartTime(LocalTime.parse("11:25"));

    final FileOutputStream os = new FileOutputStream(new File("target/andrea-frauenfeld.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, false, false, false);
    } finally {
      os.close();
    }
  }

  @Test
  public void makeBigi() throws DocumentException, IOException {
    final MarathonData data = makeHorseData();
    data.getPhaseA().setStartTime(LocalTime.parse("13:05"));
    data.getPhaseD().setStartTime(LocalTime.parse("13:28"));
    data.getPhaseE().setStartTime(LocalTime.parse("13:54"));

    final FileOutputStream os = new FileOutputStream(new File("target/bigi-frauenfeld.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, false, false, false);
    } finally {
      os.close();
    }
  }

  @Test
  public void makeChristof() throws DocumentException, IOException {
    final MarathonData data = makePonyData();
    data.getPhaseA().setStartTime(LocalTime.parse("10:44"));
    data.getPhaseD().setStartTime(LocalTime.parse("11:09"));
    data.getPhaseE().setStartTime(LocalTime.parse("11:37"));

    final FileOutputStream os = new FileOutputStream(new File("target/christof-frauenfeld.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, false, false, false);
    } finally {
      os.close();
    }
  }

  private MarathonData makeHorseData() {
    final PhaseData phaseA = new PhaseData();
    phaseA.setMaxTime(Duration.standardSeconds(60 * 21 + 36));
    phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseA.setLength(5400);
    phaseA.setVelocity(Double.valueOf(15));
    phaseA.setDefaultPoints();

    final PhaseData phaseD = new PhaseData();
    // phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseD.setMaxTime(Duration.standardSeconds(60 * 6 + 51));
    phaseD.setLength(800);
    phaseD.setVelocity(Double.valueOf(7));
    phaseD.setDefaultPoints();

    final PhaseData phaseE = new PhaseData();
    // phaseE.setStartTime(LocalTime.parse("11:35"));
    phaseE.setLength(Integer.valueOf(6280));
    phaseE.setMaxTime(Duration.standardSeconds(60 * 26 + 55));
    phaseE.setMinTime(phaseE.getMaxTime().minus(Duration.standardMinutes(3)));
    phaseE.setVelocity(Double.valueOf(14));
    phaseE.setDefaultPoints();

    final MarathonData data = new MarathonData(phaseA, phaseD, phaseE);
    return data;
  }

  private MarathonData makePonyData() {
    final PhaseData phaseA = new PhaseData();
    phaseA.setMaxTime(Duration.standardSeconds(60 * 23 + 9));
    phaseA.setMinTime(phaseA.getMaxTime().minus(Duration.standardMinutes(2)));
    phaseA.setLength(5400);
    phaseA.setVelocity(Double.valueOf(14));
    phaseA.setDefaultPoints();

    final PhaseData phaseD = new PhaseData();
    // phaseD.setStartTime(LocalTime.parse("11:20"));
    phaseD.setMaxTime(Duration.standardSeconds(60 * 8));
    phaseD.setLength(800);
    phaseD.setVelocity(Double.valueOf(6));
    phaseD.setDefaultPoints();

    final PhaseData phaseE = new PhaseData();
    // phaseE.setStartTime(LocalTime.parse("11:35"));
    phaseE.setLength(Integer.valueOf(6280));
    phaseE.setMaxTime(Duration.standardSeconds(60 * 28 + 59));
    phaseE.setMinTime(phaseE.getMaxTime().minus(Duration.standardMinutes(3)));
    phaseE.setVelocity(Double.valueOf(13));
    phaseE.setDefaultPoints();

    final MarathonData data = new MarathonData(phaseA, phaseD, phaseE);
    return data;
  }

}
