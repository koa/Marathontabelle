package ch.bergturbenthal.marathontabelle.model;

import java.io.Serializable;

public class TimeEntry implements Serializable {
  private static final long serialVersionUID = 4672068338831349222L;

  public static TimeEntry makeCommentOnlyEntry(final String comment) {
    final TimeEntry ret = new TimeEntry();
    ret.setComment(comment);
    return ret;
  }

  public static TimeEntry makePositionedEntry(final int position, final String comment) {
    return new TimeEntry(Integer.valueOf(position), comment);
  }

  public static TimeEntry makePositionedEntrySmallSheetOnly(final int position, final String comment) {
    final TimeEntry ret = new TimeEntry(Integer.valueOf(position), comment);
    ret.setOnlySmallSheet(true);
    return ret;
  }

  private Integer position;

  private String comment;

  private boolean onlySmallSheet;

  public TimeEntry() {
  }

  public TimeEntry(final Integer position, final String comment) {
    this.position = position;
    this.comment = comment;
    onlySmallSheet = false;
  }

  public String getComment() {
    return comment;
  }

  public Integer getPosition() {
    return position;
  }

  /**
   * Returns the onlySmallSheet.
   * 
   * @return the onlySmallSheet
   */
  public boolean isOnlySmallSheet() {
    return onlySmallSheet;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  /**
   * Sets the onlySmallSheet.
   * 
   * @param onlySmallSheet
   *          the onlySmallSheet to set
   */
  public void setOnlySmallSheet(final boolean onlySmallSheet) {
    this.onlySmallSheet = onlySmallSheet;
  }

  public void setPosition(final Integer position) {
    this.position = position;
  }
}
