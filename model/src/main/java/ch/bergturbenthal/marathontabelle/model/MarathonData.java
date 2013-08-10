/*
 * (c) 2012 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.model;

import java.io.Serializable;
import java.util.UUID;

public class MarathonData implements Serializable {
  private static final long serialVersionUID = -6310780878868358493L;
  private String marathonName;
  private PhaseData phaseA;
  private PhaseData phaseD;
  private PhaseData phaseE;
  private final String id = UUID.randomUUID().toString();

  public MarathonData() {

  }

  public MarathonData(final PhaseData phaseA, final PhaseData phaseD, final PhaseData phaseE) {
    this.phaseA = phaseA;
    this.phaseD = phaseD;
    this.phaseE = phaseE;
  }

  /**
   * Returns the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
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
