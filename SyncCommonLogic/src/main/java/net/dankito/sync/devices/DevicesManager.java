package net.dankito.sync.devices;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.devicediscovery.DevicesDiscovererConfig;
import net.dankito.devicediscovery.DevicesDiscovererListener;
import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.sync.Device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class DevicesManager implements IDevicesManager {

  private static final Logger log = LoggerFactory.getLogger(DevicesManager.class);


  protected IDevicesDiscoverer devicesDiscoverer;

  protected Device localDevice;

  protected ObjectMapper objectMapper = new ObjectMapper();


  protected Map<String, DiscoveredDevice> discoveredDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> knownSynchronizedDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> knownIgnoredDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> unknownDevices = new ConcurrentHashMap<>();


  protected List<DiscoveredDevicesListener> discoveredDevicesListeners = new CopyOnWriteArrayList<>();

  protected List<KnownSynchronizedDevicesListener> knownSynchronizedDevicesListeners = new CopyOnWriteArrayList<>();


  public DevicesManager(IDevicesDiscoverer devicesDiscoverer, Device localDevice) {
    this.devicesDiscoverer = devicesDiscoverer;
    this.localDevice = localDevice;
  }


  @Override
  public void start() {
    devicesDiscoverer.startAsync(new DevicesDiscovererConfig(getLocalDeviceInfo(localDevice), DevicesManagerConfig.DEVICES_DISCOVERER_PORT,
        DevicesManagerConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS, new DevicesDiscovererListener() {
      @Override
      public void deviceFound(String deviceInfo, String address) {
        getDeviceDetailsFromDevice(deviceInfo, address);
      }

      @Override
      public void deviceDisconnected(String deviceInfo) {
        DiscoveredDevice device = discoveredDevices.get(deviceInfo);
        if(device != null) {
          disconnectedFromDevice(deviceInfo, device);
        }
        else {
          log.error("This should never occur! Disconnected from Device, but was not in discoveredDevices: " + deviceInfo);
        }
      }
    }));
  }

  @Override
  public void stop() {
    devicesDiscoverer.stop();

    for(DiscoveredDevice knownSynchronizedDevice : knownSynchronizedDevices.values()) {
      callKnownSynchronizedDeviceDisconnected(knownSynchronizedDevice);
    }

    knownSynchronizedDevices.clear();
    knownIgnoredDevices.clear();
    unknownDevices.clear();


    for(DiscoveredDevice discoveredDevice : discoveredDevices.values()) {
      callKnownSynchronizedDeviceDisconnected(discoveredDevice);
    }

    discoveredDevices.clear();
  }


  protected void getDeviceDetailsFromDevice(String deviceInfo, String address) {
    // TODO

    try {
      Device device = objectMapper.readValue(deviceInfo, Device.class);
      discoveredDevice(deviceInfo, new DiscoveredDevice(device, address));
    } catch(Exception e) {
      log.error("Could not deserialize Device from " + deviceInfo, e);
    }
  }

  protected void discoveredDevice(String deviceInfo, DiscoveredDevice device) {
    synchronized(discoveredDevices) {
      discoveredDevices.put(deviceInfo, device);
      callDiscoveredDeviceConnectedListeners(device);

      if(isKnownSynchronizedDevice(device)) {
        knownSynchronizedDevices.put(deviceInfo, device);
        callKnownSynchronizedDeviceConnected(device);
      }
      else if(isKnownIgnoredDevice(device)) {
        knownIgnoredDevices.put(deviceInfo, device);
      }
      else {
        unknownDevices.put(deviceInfo, device);
      }
    }
  }

  protected void disconnectedFromDevice(String deviceInfo, DiscoveredDevice device) {
    synchronized(discoveredDevices) {
      discoveredDevices.remove(deviceInfo);

      if(isKnownSynchronizedDevice(device)) {
        knownSynchronizedDevices.remove(deviceInfo);
        callKnownSynchronizedDeviceDisconnected(device);
      }
      else if(isKnownIgnoredDevice(device)) {
        knownIgnoredDevices.remove(deviceInfo);
      }
      else {
        unknownDevices.remove(deviceInfo);
      }

      callDiscoveredDeviceDisconnectedListeners(device);
    }
  }


  protected boolean isKnownSynchronizedDevice(DiscoveredDevice device) {
    return false; // TODO
  }

  protected boolean isKnownIgnoredDevice(DiscoveredDevice device) {
    return false; // TODO
  }


  protected String getLocalDeviceInfo(Device localDevice) {
    try {
      return objectMapper.writeValueAsString(localDevice);
    } catch(Exception e) {
      log.error("Could not serialize localDevice to JSON: " + localDevice, e);
    }

    return "";
  }


  @Override
  public boolean addDiscoveredDevicesListener(DiscoveredDevicesListener listener) {
    return discoveredDevicesListeners.add(listener);
  }

  @Override
  public boolean removeDiscoveredDevicesListener(DiscoveredDevicesListener listener) {
    return discoveredDevicesListeners.remove(listener);
  }

  protected void callDiscoveredDeviceConnectedListeners(DiscoveredDevice device) {
    for(DiscoveredDevicesListener listener : discoveredDevicesListeners) {
      listener.deviceDiscovered(device);
    }
  }

  protected void callDiscoveredDeviceDisconnectedListeners(DiscoveredDevice device) {
    for(DiscoveredDevicesListener listener : discoveredDevicesListeners) {
      listener.disconnectedFromDevice(device);
    }
  }


  @Override
  public boolean addKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener) {
    return knownSynchronizedDevicesListeners.add(listener);
  }

  @Override
  public boolean removeKnownSynchronizedDevicesListener(KnownSynchronizedDevicesListener listener) {
    return knownSynchronizedDevicesListeners.remove(listener);
  }

  protected void callKnownSynchronizedDeviceConnected(DiscoveredDevice device) {
    for(KnownSynchronizedDevicesListener listener : knownSynchronizedDevicesListeners) {
      listener.knownSynchronizedDeviceConnected(device);
    }
  }

  protected void callKnownSynchronizedDeviceDisconnected(DiscoveredDevice device) {
    for(KnownSynchronizedDevicesListener listener : knownSynchronizedDevicesListeners) {
      listener.knownSynchronizedDeviceDisconnected(device);
    }
  }

}
