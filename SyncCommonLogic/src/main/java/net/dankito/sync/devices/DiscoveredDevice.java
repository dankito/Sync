package net.dankito.sync.devices;

import net.dankito.sync.Device;


public class DiscoveredDevice {

  protected Device device;

  protected String address;


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


  @Override
  public String toString() {
    return "" + getDevice();
  }

}
