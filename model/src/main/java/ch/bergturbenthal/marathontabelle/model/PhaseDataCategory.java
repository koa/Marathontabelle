package ch.bergturbenthal.marathontabelle.model;

import lombok.Data;

import org.joda.time.Duration;

@Data
public class PhaseDataCategory {
  private Duration minTime;
  private Duration maxTime;
  private Double velocity;

}
