package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;


@Entity(name = DatabaseTableConfig.LOCAL_CONFIG_TABLE_NAME)
public class LocalConfig extends BaseEntity {

  @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST } )
  @JoinColumn(name = DatabaseTableConfig.LOCAL_CONFIG_LOCAL_DEVICE_ID_JOIN_COLUMN_NAME)
  protected Device localDevice;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = DatabaseTableConfig.LOCAL_CONFIG_SYNCHRONIZED_DEVICES_JOIN_TABLE_NAME,
      joinColumns = { @JoinColumn(name = DatabaseTableConfig.LOCAL_CONFIG_SYNCHRONIZED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME) },
      inverseJoinColumns = { @JoinColumn(name = DatabaseTableConfig.LOCAL_CONFIG_SYNCHRONIZED_DEVICES_DEVICE_ID_COLUMN_NAME) }
  )
  protected List<Device> synchronizedDevices = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = DatabaseTableConfig.LOCAL_CONFIG_IGNORED_DEVICES_JOIN_TABLE_NAME,
      joinColumns = { @JoinColumn(name = DatabaseTableConfig.LOCAL_CONFIG_IGNORED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME) },
      inverseJoinColumns = { @JoinColumn(name = DatabaseTableConfig.LOCAL_CONFIG_IGNORED_DEVICES_DEVICE_ID_COLUMN_NAME) }
  )
  protected List<Device> ignoredDevices = new ArrayList<>();


  protected LocalConfig() { // for reflection

  }

  public LocalConfig(Device localDevice) {
    this.localDevice = localDevice;
  }


  public Device getLocalDevice() {
    return localDevice;
  }

  public List<Device> getSynchronizedDevices() {
    return synchronizedDevices;
  }

  public boolean addSynchronizedDevice(Device device) {
    return synchronizedDevices.add(device);
  }

  public boolean removeSynchronizedDevice(Device device) {
    return synchronizedDevices.remove(device);
  }

  public List<Device> getIgnoredDevices() {
    return ignoredDevices;
  }

  public boolean addIgnoredDevice(Device device) {
    return ignoredDevices.add(device);
  }

  public boolean removeIgnoredDevice(Device device) {
    return ignoredDevices.remove(device);
  }


  @Override
  public String toString() {
    return "" + getLocalDevice();
  }

}
