package net.dankito.devicediscovery;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesDiscovererListener {

  void deviceFound(String deviceInfo, String address);

  void deviceDisconnected(String deviceInfo);

}
