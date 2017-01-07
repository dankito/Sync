package net.dankito.sync;

/**
 * Created by ganymed on 07/01/17.
 */

public class Device extends BaseEntity {

  protected String uniqueDeviceId;

  protected OsType osType;

  protected String osName;

  protected String osVersion;

  protected String description;


  public String getUniqueDeviceId() {
    return uniqueDeviceId;
  }

  public void setUniqueDeviceId(String uniqueDeviceId) {
    this.uniqueDeviceId = uniqueDeviceId;
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
