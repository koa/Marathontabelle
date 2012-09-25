/*
 * (c) 2012 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.model;

public class MarathonData {
  private String marathonName;
  private PhaseData phaseA;
  private PhaseData phaseD;
  private PhaseData phaseE;

  public MarathonData() {

  }

  public MarathonData(final PhaseData phaseA, final PhaseData phaseD, final PhaseData phaseE) {
    this.phaseA = phaseA;
    this.phaseD = phaseD;
    this.phaseE = phaseE;
  }

  /**
   * Returns the marathonName.
   * 
   * @return the marathonName
   */
  public String getMarathonName() {
    return marathonName;
  }

  /**
   * Returns the phaseA.
   * 
   * @return the phaseA
   */
  public PhaseData getPhaseA() {
    return phaseA;
  }

  /**
   * Returns the phaseD.
   * 
   * @return the phaseD
   */
  public PhaseData getPhaseD() {
    return phaseD;
  }

  /**
   * Returns the phaseE.
   * 
   * @return the phaseE
   */
  public PhaseData getPhaseE() {
    return phaseE;
  }

  /**
   * Sets the marathonName.
   * 
   * @param marathonName
   *          the marathonName to set
   */
  public void setMarathonName(final String marathonName) {
    this.marathonName = marathonName;
  }

  /**
   * Sets the phaseA.
   * 
   * @param phaseA
   *          the phaseA to set
   */
  public void setPhaseA(final PhaseData phaseA) {
    this.phaseA = phaseA;
  }

  /**
   * Sets the phaseD.
   * 
   * @param phaseD
   *          the phaseD to set
   */
  public void setPhaseD(final PhaseData phaseD) {
    this.phaseD = phaseD;
  }

  /**
   * Sets the phaseE.
   * 
   * @param phaseE
   *          the phaseE to set
   */
  public void setPhaseE(final PhaseData phaseE) {
    this.phaseE = phaseE;
  }

}
