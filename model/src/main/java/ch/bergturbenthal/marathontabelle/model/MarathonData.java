/*
 * (c) 2012 panter llc, Zurich, Switzerland.
 */
package ch.bergturbenthal.marathontabelle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class MarathonData implements Serializable {
  private static final long serialVersionUID = -6310780878868358493L;
  private String marathonName;
  private final Map<Phase, PhaseDataCompetition> competitionPhases = new HashMap<Phase, PhaseDataCompetition>();
  private final List<String> categories = new ArrayList<String>();
  private final Map<String, DriverData> drivers = new HashMap<String, DriverData>();

  private final String id = UUID.randomUUID().toString();
}
