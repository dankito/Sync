package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@Entity(name = DatabaseTableConfig.DEVICE_TABLE_NAME)
public class Device extends BaseEntity {

  @Column(name = DatabaseTableConfig.DEVICE_UNIQUE_DEVICE_ID_COLUMN_NAME)
  protected String uniqueDeviceId;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = DatabaseTableConfig.DEVICE_OS_TYPE_COLUMN_NAME)
  protected OsType osType;

  @Column(name = DatabaseTableConfig.DEVICE_OS_NAME_TYPE_COLUMN_NAME)
  protected String osName;

  @Column(name = DatabaseTableConfig.DEVICE_OS_VERSION_TYPE_COLUMN_NAME)
  protected String osVersion;

  @Column(name = DatabaseTableConfig.DEVICE_DESCRIPTION_COLUMN_NAME)
  protected String description;


  protected Device() { // for reflection

  }

  public Device(String uniqueDeviceId) {
    this.uniqueDeviceId = uniqueDeviceId;
  }


  public String getUniqueDeviceId() {
    return uniqueDeviceId;
  }

  public OsType getOsType() {
    return osType;
  }

  public void setOsType(OsType osType) {
    this.osType = osType;
  }

  public String getOsName() {
    return osName;
  }

  public void setOsName(String osName) {
    this.osName = osName;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public String toString() {
    return getOsName() + " " + getOsVersion();
  }

}
