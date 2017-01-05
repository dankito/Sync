package net.dankito.sync;

import java.util.Date;

/**
 * Created by ganymed on 05/01/17.
 */

public class CallLogEntity extends Entity {

  protected String number;

  protected String normalizedNumber;

  protected String associatedContactName;

  protected Date date;

  protected int durationInSeconds;

  protected CallType type;


  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getNormalizedNumber() {
    return normalizedNumber;
  }

  public void setNormalizedNumber(String normalizedNumber) {
    this.normalizedNumber = normalizedNumber;
  }

  public String getAssociatedContactName() {
    return associatedContactName;
  }

  public void setAssociatedContactName(String associatedContactName) {
    this.associatedContactName = associatedContactName;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getDurationInSeconds() {
    return durationInSeconds;
  }

  public void setDurationInSeconds(int durationInSeconds) {
    this.durationInSeconds = durationInSeconds;
  }

  public CallType getType() {
    return type;
  }

  public void setType(CallType type) {
    this.type = type;
  }
}
