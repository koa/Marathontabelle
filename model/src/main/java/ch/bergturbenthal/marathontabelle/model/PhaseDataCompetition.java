package ch.bergturbenthal.marathontabelle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PhaseDataCompetition implements Serializable {
  private static final long serialVersionUID = 1339840303760291890L;
  private Integer length;
  private String phaseName;
  private String imageName;
  private final Map<String, PhaseDataCategory> categoryTimes = new HashMap<String, PhaseDataCategory>();
  private final List<TimeEntry> entries = new ArrayList<TimeEntry>();

  public void setDefaultPoints() {
    if (length != null) {
      entries.clear();
      for (int i = 1; i <= (length.intValue() - 1) / 1000; i += 1) {
        final TimeEntry e = new TimeEntry();
        e.setPosition(Integer.valueOf(i * 1000));
        e.setComment("km" + i);
        entries.add(e);
      }
      final TimeEntry endEntry = new TimeEntry();
      endEntry.setPosition(length);
      endEntry.setComment("Finish");
      entries.add(endEntry);
    }
  }
}
