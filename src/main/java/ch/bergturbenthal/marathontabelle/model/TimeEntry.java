package ch.bergturbenthal.marathontabelle.model;

public class TimeEntry {
  private Integer position;
  private String comment;

  public TimeEntry() {
  }

  public TimeEntry(final Integer position, final String comment) {
    this.position = position;
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }

  public Integer getPosition() {
    return position;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setPosition(final Integer position) {
    this.position = position;
  }
}
