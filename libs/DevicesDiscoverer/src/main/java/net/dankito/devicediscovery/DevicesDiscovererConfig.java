package net.dankito.devicediscovery;

/**
 * Created by ganymed on 06/01/17.
 */

public class DevicesDiscovererConfig {

  public static final String DEFAULT_DISCOVERY_MESSAGE_PREFIX = "DevicesDiscovery";


  protected String localDeviceInfo;

  protected int discoverDevicesPort;

  protected int checkForDevicesIntervalMillis;

  protected String discoveryMessagePrefix = DEFAULT_DISCOVERY_MESSAGE_PREFIX;

  protected DevicesDiscovererListener listener;


  public DevicesDiscovererConfig() {

  }

  public DevicesDiscovererConfig(String localDeviceInfo, int discoverDevicesPort, int checkForDevicesIntervalMillis, DevicesDiscovererListener listener) {
    this.localDeviceInfo = localDeviceInfo;
    this.discoverDevicesPort = discoverDevicesPort;
    this.checkForDevicesIntervalMillis = checkForDevicesIntervalMillis;
    this.listener = listener;
  }

  public DevicesDiscovererConfig(String localDeviceInfo, int discoverDevicesPort, int checkForDevicesIntervalMillis, String discoveryMessagePrefix, DevicesDiscovererListener listener) {
    this(localDeviceInfo, discoverDevicesPort, checkForDevicesIntervalMillis, listener);

    this.discoveryMessagePrefix = discoveryMessagePrefix;
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

  public String getDiscoveryMessagePrefix() {
    return discoveryMessagePrefix;
  }

  public void setDiscoveryMessagePrefix(String discoveryMessagePrefix) {
    this.discoveryMessagePrefix = discoveryMessagePrefix;
  }

  public DevicesDiscovererListener getListener() {
    return listener;
  }

  public void setListener(DevicesDiscovererListener listener) {
    this.listener = listener;
  }


  @Override
  public String toString() {
    return getLocalDeviceInfo();
  }

}
