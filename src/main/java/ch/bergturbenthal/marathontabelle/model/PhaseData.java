package ch.bergturbenthal.marathontabelle.model;

import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.Duration;
import org.joda.time.LocalTime;

public class PhaseData {
  private LocalTime startTime;
  private Integer length;
  private Duration minTime;
  private Duration maxTime;
  private Double velocity;
  private Collection<TimeEntry> entries;

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
      for (int i = 1000; i < length.intValue(); i += 1000) {
        final TimeEntry e = new TimeEntry();
        e.setPosition(Integer.valueOf(i));
        entries.add(e);
      }
      final TimeEntry endEntry = new TimeEntry();
      endEntry.setPosition(length);
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
