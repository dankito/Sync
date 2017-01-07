package net.dankito.sync;

import java.util.Date;

/**
 * Created by ganymed on 05/01/17.
 */

public class ImageFileSyncEntity extends FileSyncEntity {

  protected int height;

  protected int width;

  protected double latitude;

  protected double longitude;

  protected Date imageTakenOn;

  protected int orientation;


  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public Date getImageTakenOn() {
    return imageTakenOn;
  }

  public void setImageTakenOn(Date imageTakenOn) {
    this.imageTakenOn = imageTakenOn;
  }

  public int getOrientation() {
    return orientation;
  }

  public void setOrientation(int orientation) {
    this.orientation = orientation;
  }

}
