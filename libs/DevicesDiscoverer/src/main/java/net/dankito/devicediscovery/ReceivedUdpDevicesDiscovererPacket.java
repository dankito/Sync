package net.dankito.devicediscovery;

import java.net.DatagramPacket;

/**
 * Created by ganymed on 20/10/16.
 */
public class ReceivedUdpDevicesDiscovererPacket {

  protected byte[] receivedData;

  protected DatagramPacket packet;

  protected String senderAddress;

  protected String localDeviceInfo;

  protected IDevicesDiscovererListener listener;


  public ReceivedUdpDevicesDiscovererPacket(byte[] receivedData, DatagramPacket packet, String senderAddress, String localDeviceInfo, IDevicesDiscovererListener listener) {
    this.receivedData = receivedData;
    this.packet = packet;
    this.senderAddress = senderAddress;
    this.localDeviceInfo = localDeviceInfo;
    this.listener = listener;
  }


  public byte[] getReceivedData() {
    return receivedData;
  }

  public DatagramPacket getPacket() {
    return packet;
  }

  public String getSenderAddress() {
    return senderAddress;
  }

  public String getLocalDeviceInfo() {
    return localDeviceInfo;
  }

  public IDevicesDiscovererListener getListener() {
    return listener;
  }

}
