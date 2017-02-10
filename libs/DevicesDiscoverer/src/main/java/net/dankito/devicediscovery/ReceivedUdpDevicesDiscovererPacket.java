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

  protected String discoveryMessagePrefix;

  protected DevicesDiscovererListener listener;


  public ReceivedUdpDevicesDiscovererPacket(byte[] receivedData, DatagramPacket packet, String senderAddress, String localDeviceInfo, String discoveryMessagePrefix, DevicesDiscovererListener listener) {
    this.receivedData = receivedData;
    this.packet = packet;
    this.senderAddress = senderAddress;
    this.localDeviceInfo = localDeviceInfo;
    this.discoveryMessagePrefix = discoveryMessagePrefix;
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

  public String getDiscoveryMessagePrefix() {
    return discoveryMessagePrefix;
  }

  public DevicesDiscovererListener getListener() {
    return listener;
  }

}
