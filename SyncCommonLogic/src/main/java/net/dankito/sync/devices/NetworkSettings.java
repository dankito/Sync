package net.dankito.sync.devices;

import net.dankito.sync.Device;

import javax.inject.Named;


@Named
public class NetworkSettings implements INetworkSettings {

  protected Device localHostDevice;

  protected int messagePort;

  protected int synchronizationPort;


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
    this.messagePort = messagePort;
  }

  @Override
  public int getSynchronizationPort() {
    return synchronizationPort;
  }

  @Override
  public void setSynchronizationPort(int synchronizationPort) {
    this.synchronizationPort = synchronizationPort;
  }


  @Override
  public String toString() {
    return getLocalHostDevice() + ", Messages Port: " + getMessagePort() + ", Synchronization Port: " + getSynchronizationPort();
  }

}
