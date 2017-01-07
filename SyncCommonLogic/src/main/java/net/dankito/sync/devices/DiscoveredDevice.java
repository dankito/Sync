package net.dankito.sync.devices;

import net.dankito.sync.Device;


public class DiscoveredDevice {

  protected Device device;

  protected String address;

  protected int messagesPort;

  protected int synchronizationPort;


  public DiscoveredDevice(Device device, String address) {
    this.device = device;
    this.address = address;
  }


  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getMessagesPort() {
    return messagesPort;
  }

  public void setMessagesPort(int messagesPort) {
    this.messagesPort = messagesPort;
  }

  public int getSynchronizationPort() {
    return synchronizationPort;
  }

  public void setSynchronizationPort(int synchronizationPort) {
    this.synchronizationPort = synchronizationPort;
  }


  @Override
  public String toString() {
    return "" + getDevice();
  }

}
