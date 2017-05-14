package ch.bergturbenthal.marathontabelle.model;

import java.time.Duration;

import lombok.Data;

@Data
public class PhaseDataCategory {
  private Duration minTime;
  private Duration maxTime;
  private Double velocity;

}
