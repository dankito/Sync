package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;


@Entity(name = DatabaseTableConfig.DEVICE_TABLE_NAME)
public class Device extends BaseEntity {

  @Column(name = DatabaseTableConfig.DEVICE_UNIQUE_DEVICE_ID_COLUMN_NAME)
  protected String uniqueDeviceId;

  @Column(name = DatabaseTableConfig.DEVICE_NAME_TYPE_COLUMN_NAME)
  protected String name;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = DatabaseTableConfig.DEVICE_OS_TYPE_COLUMN_NAME)
  protected OsType osType;

  @Column(name = DatabaseTableConfig.DEVICE_OS_NAME_TYPE_COLUMN_NAME)
  protected String osName;

  @Column(name = DatabaseTableConfig.DEVICE_OS_VERSION_TYPE_COLUMN_NAME)
  protected String osVersion;

  @Column(name = DatabaseTableConfig.DEVICE_DESCRIPTION_COLUMN_NAME)
  protected String description;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "sourceDevice")
  protected List<SyncConfiguration> sourceSyncConfigurations = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "destinationDevice")
  protected List<SyncConfiguration> destinationSyncConfigurations = new ArrayList<>();


  protected Device() { // for reflection

  }

  public Device(String uniqueDeviceId) {
    this.uniqueDeviceId = uniqueDeviceId;
  }


  public String getUniqueDeviceId() {
    return uniqueDeviceId;
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

  public List<SyncConfiguration> getSourceSyncConfigurations() {
    return sourceSyncConfigurations;
  }

  public boolean addSourceSyncConfiguration(SyncConfiguration syncConfiguration) {
    return sourceSyncConfigurations.add(syncConfiguration);
  }

  public boolean removeSourceSyncConfiguration(SyncConfiguration syncConfiguration) {
    return sourceSyncConfigurations.remove(syncConfiguration);
  }

  public List<SyncConfiguration> getDestinationSyncConfigurations() {
    return destinationSyncConfigurations;
  }

  public boolean addDestinationSyncConfiguration(SyncConfiguration syncConfiguration) {
    return destinationSyncConfigurations.add(syncConfiguration);
  }

  public boolean removeDestinationSyncConfiguration(SyncConfiguration syncConfiguration) {
    return destinationSyncConfigurations.remove(syncConfiguration);
  }


  public String getDeviceDisplayName() {
    return getOsType() == OsType.ANDROID ? getName() : getOsName();
  }

  public String getDeviceFullDisplayName() {
    String fullDisplayName = getOsName() + " " + getOsVersion();

    if(getName() != null && getName().length() > 0) {
      fullDisplayName = getName() + " " + fullDisplayName;
    }

    return fullDisplayName;
  }


  @Override
  public String toString() {
    String description = getOsName() + " " + getOsVersion();

    if(name != null && name.length() > 0) {
      description = name + " " + description;
    }

    return description;
  }

}
