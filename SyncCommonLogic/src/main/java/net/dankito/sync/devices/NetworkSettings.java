package net.dankito.sync.devices;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.data.IDataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;
import javax.inject.Named;


@Named
public class NetworkSettings implements INetworkSettings {

  protected LocalConfig localConfig;

  protected Device localHostDevice;

  protected int messagePort;

  protected int synchronizationPort;

  protected Map<String, DiscoveredDevice> discoveredDevices = new HashMap<>();

  protected Map<String, DiscoveredDevice> connectedDevicesPermittedToSynchronization = new HashMap<>();

  protected Map<String, DiscoveredDevice> devicesAskedForPermittingSynchronization = new HashMap<>();

  protected List<NetworkSettingsChangedListener> listeners = new CopyOnWriteArrayList<>();


  @Inject
  public NetworkSettings(IDataManager dataManager) {
    this(dataManager.getLocalConfig());
  }

  public NetworkSettings(LocalConfig localConfig) {
    this.localConfig = localConfig;
    this.localHostDevice = localConfig.getLocalDevice();
  }


  @Override
  public LocalConfig getLocalConfig() {
    return localConfig;
  }

  @Override
  public Device getLocalHostDevice() {
    return localHostDevice;
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


  public DiscoveredDevice getDiscoveredDevice(String uniqueDeviceId) {
    return discoveredDevices.get(uniqueDeviceId);
  }

  public void addDiscoveredDevice(DiscoveredDevice device) {
    discoveredDevices.put(device.getDevice().getUniqueDeviceId(), device);

    callSettingChangedListeners(NetworkSetting.ADDED_DISCOVERED_DEVICE, device, null);
  }

  public void removeDiscoveredDevice(DiscoveredDevice device) {
    discoveredDevices.remove(device.getDevice().getUniqueDeviceId());

    callSettingChangedListeners(NetworkSetting.REMOVED_DISCOVERED_DEVICE, device, null);
  }


  public void addConnectedDevicePermittedToSynchronize(DiscoveredDevice device) {
    connectedDevicesPermittedToSynchronization.put(device.getDevice().getUniqueDeviceId(), device);

    callSettingChangedListeners(NetworkSetting.ADDED_CONNECTED_DEVICE_PERMITTED_TO_SYNCHRONIZE, device, null);
  }

  public void removeConnectedDevicePermittedToSynchronize(DiscoveredDevice device) {
    connectedDevicesPermittedToSynchronization.remove(device.getDevice().getUniqueDeviceId());

    callSettingChangedListeners(NetworkSetting.REMOVED_CONNECTED_DEVICE_PERMITTED_TO_SYNCHRONIZE, device, null);
  }


  public void addDevicesAskedForPermittingSynchronization(DiscoveredDevice device) {
    devicesAskedForPermittingSynchronization.put(device.getDevice().getUniqueDeviceId(), device);

    callSettingChangedListeners(NetworkSetting.ADDED_DEVICES_ASKED_FOR_PERMITTING_SYNCHRONIZATION, device, null);
  }

  public void removeDevicesAskedForPermittingSynchronization(DiscoveredDevice device) {
    devicesAskedForPermittingSynchronization.remove(device.getDevice().getUniqueDeviceId());

    callSettingChangedListeners(NetworkSetting.REMOVED_DEVICES_ASKED_FOR_PERMITTING_SYNCHRONIZATION, device, null);
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
