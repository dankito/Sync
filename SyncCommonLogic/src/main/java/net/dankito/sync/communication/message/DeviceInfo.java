package net.dankito.sync.communication.message;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;


public class DeviceInfo {

  protected String id;

  protected String uniqueDeviceId;

  protected String name;

  protected OsType osType;

  protected String osName;

  protected String osVersion;

  protected String description;


  public DeviceInfo() {

  }

  public DeviceInfo(String id, String uniqueDeviceId, String name, OsType osType, String osName, String osVersion, String description) {
    this.id = id;
    this.uniqueDeviceId = uniqueDeviceId;
    this.name = name;
    this.osType = osType;
    this.osName = osName;
    this.osVersion = osVersion;
    this.description = description;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUniqueDeviceId() {
    return uniqueDeviceId;
  }

  public void setUniqueDeviceId(String uniqueDeviceId) {
    this.uniqueDeviceId = uniqueDeviceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
    return getName() + " " + getOsName();
  }


  public static DeviceInfo fromDevice(Device device) {
    return new DeviceInfo(device.getId(), device.getUniqueDeviceId(), device.getName(), device.getOsType(), device.getOsName(), device.getOsVersion(), device.getDescription());
  }

}
