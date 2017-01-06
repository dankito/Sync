package net.dankito.devicediscovery;

/**
 * Created by ganymed on 05/06/16.
 */
public interface IDevicesDiscoverer {

  boolean isRunning();

  void startAsync(DevicesDiscovererConfig config);

  void stop();

  void disconnectedFromDevice(String deviceInfo);

}
