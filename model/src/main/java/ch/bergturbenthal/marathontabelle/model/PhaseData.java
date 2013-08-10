package ch.bergturbenthal.marathontabelle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.Duration;
import org.joda.time.LocalTime;

public class PhaseData implements Serializable {
  private static final long serialVersionUID = 1339840303760291890L;
  private LocalTime startTime;
  private Integer length;
  private Duration minTime;
  private Duration maxTime;
  private Double velocity;
  private Collection<TimeEntry> entries = new ArrayList<TimeEntry>();

  public Collection<TimeEntry> getEntries() {
    return entries;
  }

  public Integer getLength() {
    return length;
  }

  public Duration getMaxTime() {
    return maxTime;
  }

  public Duration getMinTime() {
    return minTime;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public Double getVelocity() {
    return velocity;
  }

  public void setDefaultPoints() {
    if (length != null) {
      entries = new ArrayList<TimeEntry>();
      for (int i = 1; i < length.intValue() / 1000; i += 1) {
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

  public void setEntries(final Collection<TimeEntry> entries) {
    this.entries = entries;
  }

  public void setLength(final Integer length) {
    this.length = length;
  }

  public void setMaxTime(final Duration maxTime) {
    this.maxTime = maxTime;
  }

  public void setMinTime(final Duration minTime) {
    this.minTime = minTime;
  }

  public void setStartTime(final LocalTime startTime) {
    this.startTime = startTime;
  }

  public void setVelocity(final Double velocity) {
    this.velocity = velocity;
  }
}
