package net.dankito.sync.devices;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.data.IDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;
import javax.inject.Named;


@Named
public class NetworkSettings implements INetworkSettings {

  protected Device localHostDevice;

  protected int messagePort;

  protected int synchronizationPort;

  protected Map<String, Device> devicesPermittedSynchronization = new HashMap<>();

  protected List<NetworkSettingsChangedListener> listeners = new CopyOnWriteArrayList<>();


  @Inject
  public NetworkSettings(IDataManager dataManager) {
    this(dataManager.getLocalConfig());
  }

  public NetworkSettings(LocalConfig localConfig) {
    initProperties(localConfig);
  }


  private void initProperties(LocalConfig localConfig) {
    this.localHostDevice = localConfig.getLocalDevice();

    for(Device synchronizedDevice : localConfig.getSynchronizedDevices()) {
      addDevicePermittedToSynchronize(synchronizedDevice);
    }
  }


  @Override
  public Device getLocalHostDevice() {
    return localHostDevice;
  }

  public void setLocalHostDevice(Device localHostDevice) {
    this.localHostDevice = localHostDevice;
  }

  @Override
  public int getMessagePort() {
    return messagePort;
  }

  @Override
  public void setMessagePort(int messagePort) {
    Object oldValue = this.messagePort;

    this.messagePort = messagePort;

    callSettingChangedListeners(NetworkSetting.MESSAGES_PORT, messagePort, oldValue);
  }

  @Override
  public int getSynchronizationPort() {
    return synchronizationPort;
  }

  @Override
  public void setSynchronizationPort(int synchronizationPort) {
    Object oldValue = this.synchronizationPort;

    this.synchronizationPort = synchronizationPort;

    callSettingChangedListeners(NetworkSetting.SYNCHRONIZATION_PORT, synchronizationPort, oldValue);
  }


  public boolean isDevicePermittedToSynchronize(String uniqueDeviceId) {
    return devicesPermittedSynchronization.containsKey(uniqueDeviceId);
  }

  public void addDevicePermittedToSynchronize(Device device) {
    List<Device> oldValue = new ArrayList<>(devicesPermittedSynchronization.values());

    devicesPermittedSynchronization.put(device.getUniqueDeviceId(), device);

    List<Device> newValue = new ArrayList<>(devicesPermittedSynchronization.values());
    callSettingChangedListeners(NetworkSetting.ADDED_DEVICE_PERMITTED_TO_SYNCHRONIZE, newValue, oldValue);
  }

  public void removeDevicePermittedToSynchronize(Device device) {
    List<Device> oldValue = new ArrayList<>(devicesPermittedSynchronization.values());

    devicesPermittedSynchronization.remove(device.getUniqueDeviceId());

    List<Device> newValue = new ArrayList<>(devicesPermittedSynchronization.values());
    callSettingChangedListeners(NetworkSetting.REMOVED_DEVICE_PERMITTED_TO_SYNCHRONIZE, newValue, oldValue);
  }


  @Override
  public void addListener(NetworkSettingsChangedListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(NetworkSettingsChangedListener listener) {
    listeners.remove(listener);
  }

  protected void callSettingChangedListeners(NetworkSetting setting, Object newValue, Object oldValue) {
    for(NetworkSettingsChangedListener listener : listeners) {
      listener.settingsChanged(this, setting, newValue, oldValue);
    }
  }


  @Override
  public String toString() {
    return getLocalHostDevice() + ", Messages Port: " + getMessagePort() + ", Synchronization Port: " + getSynchronizationPort();
  }

}
