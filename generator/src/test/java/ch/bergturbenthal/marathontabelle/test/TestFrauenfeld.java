/*
 * (c) 2012 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

import org.junit.Test;

import com.itextpdf.text.DocumentException;

import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.DriverData;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCategory;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCompetition;

/**
 * TODO: add type comment.
 *
 */
public class TestFrauenfeld {

  @Test
  public void makeAndrea() throws DocumentException, IOException {
    final MarathonData data = makeCompetitionData();
    final FileOutputStream os = new FileOutputStream(new File("target/andrea-frauenfeld.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, "Andrea Dietiker");
    } finally {
      os.close();
    }
  }

  @Test
  public void makeBigi() throws DocumentException, IOException {
    final MarathonData data = makeCompetitionData();
    final FileOutputStream os = new FileOutputStream(new File("target/bigi-frauenfeld.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, "Brigitte Spörri");
    } finally {
      os.close();
    }

  }

  @Test
  public void makeChristof() throws DocumentException, IOException {

    final MarathonData data = makeCompetitionData();
    final FileOutputStream os = new FileOutputStream(new File("target/christof-frauenfeld.pdf"));
    try {
      new GeneratePdf().makePdf(os, data, "Christof König");
    } finally {
      os.close();
    }

  }

  private MarathonData makeCompetitionData() {
    final MarathonData data = new MarathonData();
    final PhaseDataCompetition phaseA = new PhaseDataCompetition();
    final String categoryPony = "Pony 2 Spänner";
    final String categoryHorse = "2 Spänner";
    data.getCategories().add(categoryPony);
    data.getCategories().add(categoryHorse);
    data.getCompetitionPhases().put(Phase.A, phaseA);
    final PhaseDataCategory phaseADataCategoryPony = new PhaseDataCategory();
    final PhaseDataCategory phaseADataCategoryHorse = new PhaseDataCategory();
    phaseA.getCategoryTimes().put(categoryPony, phaseADataCategoryPony);
    phaseA.getCategoryTimes().put(categoryHorse, phaseADataCategoryHorse);

    phaseA.setPhaseName("Phase A");

    phaseADataCategoryPony.setMaxTime(Duration.ofSeconds(60 * 23 + 9));
    phaseADataCategoryPony.setMinTime(phaseADataCategoryPony.getMaxTime().minus(Duration.ofMinutes(2)));
    phaseADataCategoryPony.setVelocity(Double.valueOf(14));

    phaseADataCategoryHorse.setMaxTime(Duration.ofSeconds(60 * 21 + 36));
    phaseADataCategoryHorse.setMinTime(phaseADataCategoryHorse.getMaxTime().minus(Duration.ofMinutes(2)));
    phaseADataCategoryHorse.setVelocity(Double.valueOf(15));

    phaseA.setLength(5400);
    phaseA.setDefaultPoints();

    final PhaseDataCompetition phaseD = new PhaseDataCompetition();
    data.getCompetitionPhases().put(Phase.TRANSFER, phaseD);
    final PhaseDataCategory phaseDDataCategoryPony = new PhaseDataCategory();
    final PhaseDataCategory phaseDDataCategoryHorse = new PhaseDataCategory();
    phaseD.getCategoryTimes().put(categoryPony, phaseDDataCategoryPony);
    phaseD.getCategoryTimes().put(categoryHorse, phaseDDataCategoryHorse);

    phaseDDataCategoryPony.setMaxTime(Duration.ofSeconds(60 * 8));
    phaseDDataCategoryPony.setVelocity(Double.valueOf(6));
    phaseDDataCategoryHorse.setMaxTime(Duration.ofSeconds(60 * 6 + 51));
    phaseDDataCategoryHorse.setVelocity(Double.valueOf(7));

    phaseD.setPhaseName("Transfer");
    phaseD.setLength(800);
    phaseD.setDefaultPoints();

    final PhaseDataCompetition phaseE = new PhaseDataCompetition();
    data.getCompetitionPhases().put(Phase.B, phaseE);
    final PhaseDataCategory phaseEDataCategoryPony = new PhaseDataCategory();
    final PhaseDataCategory phaseEDataCategoryHorse = new PhaseDataCategory();
    phaseE.getCategoryTimes().put(categoryPony, phaseEDataCategoryPony);
    phaseE.getCategoryTimes().put(categoryHorse, phaseEDataCategoryHorse);

    phaseEDataCategoryPony.setMaxTime(Duration.ofSeconds(60 * 28 + 59));
    phaseEDataCategoryPony.setMinTime(phaseEDataCategoryPony.getMaxTime().minus(Duration.ofMinutes(3)));
    phaseEDataCategoryPony.setVelocity(Double.valueOf(13));

    phaseEDataCategoryHorse.setMaxTime(Duration.ofSeconds(60 * 26 + 55));
    phaseEDataCategoryHorse.setMinTime(phaseEDataCategoryHorse.getMaxTime().minus(Duration.ofMinutes(3)));
    phaseEDataCategoryHorse.setVelocity(Double.valueOf(14));

    phaseE.setPhaseName("Phase E");
    phaseE.setLength(Integer.valueOf(6280));
    phaseE.setDefaultPoints();

    // Andrea
    final DriverData andreaData = new DriverData();
    data.getDrivers().put("Andreas Dietiker", andreaData);
    andreaData.setCategory(categoryPony);
    andreaData.getStartTimes().put(Phase.A, LocalTime.parse("10:32"));
    andreaData.getStartTimes().put(Phase.TRANSFER, LocalTime.parse("10:57"));
    andreaData.getStartTimes().put(Phase.B, LocalTime.parse("11:25"));

    // Christof
    final DriverData christofData = new DriverData();
    data.getDrivers().put("Christof König", christofData);
    christofData.setCategory(categoryPony);
    christofData.getStartTimes().put(Phase.A, LocalTime.parse("10:44"));
    christofData.getStartTimes().put(Phase.TRANSFER, LocalTime.parse("11:09"));
    christofData.getStartTimes().put(Phase.B, LocalTime.parse("11:37"));

    // Bigi
    final DriverData bigiData = new DriverData();
    data.getDrivers().put("Brigitte Spörri", bigiData);
    bigiData.setCategory(categoryPony);
    bigiData.getStartTimes().put(Phase.A, LocalTime.parse("13:05"));
    bigiData.getStartTimes().put(Phase.TRANSFER, LocalTime.parse("13:28"));
    bigiData.getStartTimes().put(Phase.B, LocalTime.parse("13:54"));

    return data;

  }

}
