package ch.bergturbenthal.marathontabelle.generator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;

public class FormatUtil {
  private static final NumberFormat twoDecimal = new DecimalFormat("00");

  public static String formatDuration(final Duration d) {
    final long secondCount = d.getSeconds() + (d.getNano() > 499999999 ? 1 : 0);
    final int minutes = (int) (secondCount / 60);
    final int secondFraction = (int) (secondCount % 60);
    synchronized (twoDecimal) {
      return twoDecimal.format(minutes) + ":" + twoDecimal.format(secondFraction);
    }
  }

}
