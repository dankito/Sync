package net.dankito.devicediscovery;

/**
 * Created by ganymed on 06/01/17.
 */

public class DevicesDiscovererConfig {

  protected String localDeviceInfo;

  protected int discoverDevicesPort;

  protected int checkForDevicesIntervalMillis;

  protected IDevicesDiscovererListener listener;


  public DevicesDiscovererConfig() {

  }

  public DevicesDiscovererConfig(String localDeviceInfo, int discoverDevicesPort, int checkForDevicesIntervalMillis, IDevicesDiscovererListener listener) {
    this.localDeviceInfo = localDeviceInfo;
    this.discoverDevicesPort = discoverDevicesPort;
    this.checkForDevicesIntervalMillis = checkForDevicesIntervalMillis;
    this.listener = listener;
  }


  public String getLocalDeviceInfo() {
    return localDeviceInfo;
  }

  public void setLocalDeviceInfo(String localDeviceInfo) {
    this.localDeviceInfo = localDeviceInfo;
  }

  public int getDiscoverDevicesPort() {
    return discoverDevicesPort;
  }

  public void setDiscoverDevicesPort(int discoverDevicesPort) {
    this.discoverDevicesPort = discoverDevicesPort;
  }

  public int getCheckForDevicesIntervalMillis() {
    return checkForDevicesIntervalMillis;
  }

  public void setCheckForDevicesIntervalMillis(int checkForDevicesIntervalMillis) {
    this.checkForDevicesIntervalMillis = checkForDevicesIntervalMillis;
  }

  public IDevicesDiscovererListener getListener() {
    return listener;
  }

  public void setListener(IDevicesDiscovererListener listener) {
    this.listener = listener;
  }


  @Override
  public String toString() {
    return getLocalDeviceInfo();
  }

}
