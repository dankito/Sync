package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_TABLE_NAME)
@DiscriminatorValue(value = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_DISCRIMINATOR_VALUE)
public class ImageFileSyncEntity extends FileSyncEntity {

  @Column(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_HEIGHT_COLUMN_NAME)
  protected int height;

  @Column(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_WIDTH_COLUMN_NAME)
  protected int width;

  @Column(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_LATITUDE_COLUMN_NAME)
  protected double latitude;

  @Column(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_LONGITUDE_COLUMN_NAME)
  protected double longitude;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_IMAGE_TAKEN_ON_COLUMN_NAME)
  protected Date imageTakenOn;

  @Column(name = DatabaseTableConfig.IMAGE_FILE_SYNC_ENTITY_ORIENTATION_COLUMN_NAME)
  protected int orientation;


  protected ImageFileSyncEntity() { // for reflection

  }

  public ImageFileSyncEntity(SyncModuleConfiguration syncModuleConfiguration) {
    super(syncModuleConfiguration);
  }


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
