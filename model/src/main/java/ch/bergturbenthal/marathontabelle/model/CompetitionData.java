package ch.bergturbenthal.marathontabelle.model;

public class CompetitionData {
  private PhaseDataCompetition phaseA;
  private PhaseDataCompetition phaseD;
  private PhaseDataCompetition phaseE;

  public PhaseDataCompetition getPhaseA() {
    return phaseA;
  }

  public PhaseDataCompetition getPhaseD() {
    return phaseD;
  }

  public PhaseDataCompetition getPhaseE() {
    return phaseE;
  }

  public void setPhaseA(final PhaseDataCompetition phaseA) {
    this.phaseA = phaseA;
  }

  public void setPhaseD(final PhaseDataCompetition phaseD) {
    this.phaseD = phaseD;
  }

  public void setPhaseE(final PhaseDataCompetition phaseE) {
    this.phaseE = phaseE;
  }
}
