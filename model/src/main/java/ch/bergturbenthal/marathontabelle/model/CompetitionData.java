package ch.bergturbenthal.marathontabelle.model;

public class CompetitionData {
  private PhaseData phaseA;
  private PhaseData phaseD;
  private PhaseData phaseE;

  public PhaseData getPhaseA() {
    return phaseA;
  }

  public PhaseData getPhaseD() {
    return phaseD;
  }

  public PhaseData getPhaseE() {
    return phaseE;
  }

  public void setPhaseA(final PhaseData phaseA) {
    this.phaseA = phaseA;
  }

  public void setPhaseD(final PhaseData phaseD) {
    this.phaseD = phaseD;
  }

  public void setPhaseE(final PhaseData phaseE) {
    this.phaseE = phaseE;
  }
}
