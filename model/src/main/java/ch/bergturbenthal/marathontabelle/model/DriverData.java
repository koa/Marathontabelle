package ch.bergturbenthal.marathontabelle.model;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class DriverData {
  private String name;
  private String category;
  private final Map<Phase, LocalTime> startTimes = new HashMap<Phase, LocalTime>();
  private final Set<Phase> smallSheets = new HashSet<Phase>();
}
