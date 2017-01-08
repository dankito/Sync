package net.dankito.sync.devices;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.devicediscovery.DevicesDiscovererConfig;
import net.dankito.devicediscovery.DevicesDiscovererListener;
import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.IEntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class DevicesManager implements IDevicesManager {

  private static final Logger log = LoggerFactory.getLogger(DevicesManager.class);


  protected IDevicesDiscoverer devicesDiscoverer;

  protected LocalConfig localConfig;

  protected IEntityManager entityManager;

  protected ObjectMapper objectMapper = new ObjectMapper();


  protected Map<String, DiscoveredDevice> discoveredDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> knownSynchronizedDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> knownIgnoredDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> unknownDevices = new ConcurrentHashMap<>();


  protected List<DiscoveredDevicesListener> discoveredDevicesListeners = new CopyOnWriteArrayList<>();

  protected List<KnownSynchronizedDevicesListener> knownSynchronizedDevicesListeners = new CopyOnWriteArrayList<>();


  public DevicesManager(IDevicesDiscoverer devicesDiscoverer, IEntityManager entityManager, LocalConfig localConfig) {
    this.devicesDiscoverer = devicesDiscoverer;
    this.entityManager = entityManager;
    this.localConfig = localConfig;
  }


  @Override
  public void start() {
    devicesDiscoverer.startAsync(new DevicesDiscovererConfig(getDeviceInfoFromDeviceForDevicesDiscoverer(localConfig.getLocalDevice()), DevicesManagerConfig.DEVICES_DISCOVERER_PORT,
        DevicesManagerConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS, new DevicesDiscovererListener() {
      @Override
      public void deviceFound(String deviceInfo, String address) {
        requestDeviceDetailsFromDevice(deviceInfo, address);
      }

      @Override
      public void deviceDisconnected(String deviceInfo) {
        // TODO: normally deviceInfo is the unique device id, we still have to implement this
        try {
          Device device = objectMapper.readValue(deviceInfo, Device.class);
          deviceInfo = device.getUniqueDeviceId();
        } catch(Exception ignored) { }

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


  protected void requestDeviceDetailsFromDevice(String deviceInfo, String address) {
    // TODO

    try {
      Device device = objectMapper.readValue(deviceInfo, Device.class);

      Device persistedDevice = entityManager.getEntityById(Device.class, device.getId());
      if(persistedDevice == null) {
        entityManager.persistEntity(device);
      }
      else {
        device = persistedDevice;
      }

      deviceInfo = device.getUniqueDeviceId();

      discoveredDevice(deviceInfo, new DiscoveredDevice(device, address));
    } catch(Exception e) {
      log.error("Could not deserialize Device from " + deviceInfo, e);
    }
  }

  protected void discoveredDevice(String deviceInfo, DiscoveredDevice device) {
    synchronized(discoveredDevices) {
      discoveredDevices.put(deviceInfo, device);

      DiscoveredDeviceType type = determineDiscoveredDeviceType(device);
      callDiscoveredDeviceConnectedListeners(device, type);

      if(type == DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
        knownSynchronizedDevices.put(deviceInfo, device);
        callKnownSynchronizedDeviceConnected(device);
      }
      else if(type == DiscoveredDeviceType.KNOWN_IGNORED_DEVICE) {
        knownIgnoredDevices.put(deviceInfo, device);
      }
      else {
        unknownDevices.put(deviceInfo, device);
      }
    }
  }

  protected DiscoveredDeviceType determineDiscoveredDeviceType(DiscoveredDevice device) {
    if(isKnownSynchronizedDevice(device)) {
      return DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE;
    }
    else if(isKnownIgnoredDevice(device)) {
      return DiscoveredDeviceType.KNOWN_IGNORED_DEVICE;
    }
    else {
      return DiscoveredDeviceType.UNKNOWN_DEVICE;
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
    return localConfig.getSynchronizedDevices().contains(device.getDevice());
  }

  protected boolean isKnownIgnoredDevice(DiscoveredDevice device) {
    return localConfig.getIgnoredDevices().contains(device.getDevice());
  }


  // TODO: replace by normal call to getDeviceInfoFromDevice() as soon as Message Bus is implemented
  protected String getDeviceInfoFromDeviceForDevicesDiscoverer(Device device) {
    try {
      return objectMapper.writeValueAsString(device);
    } catch(Exception e) {
      log.error("Could not serialize device to JSON: " + device, e);
    }

    return "";
  }

  protected String getDeviceInfoFromDevice(Device device) {
    return device.getUniqueDeviceId();
  }


  @Override
  public void startSynchronizingWithDevice(DiscoveredDevice device, List<SyncModuleConfiguration> syncModuleConfigurations) {
    // TODO: the whole process should actually run in a transaction
    SyncConfiguration syncConfiguration = new SyncConfiguration(localConfig.getLocalDevice(), device.getDevice(), syncModuleConfigurations);
    if(entityManager.persistEntity(syncConfiguration)) {
      if(addDeviceToKnownSynchronizedDevices(device)) {
        addDeviceToLocalConfigSynchronizedDevices(device);

        setSourceAndDestinationSyncConfigurationOnDevices(device, syncConfiguration);

        callDiscoveredDeviceDisconnectedListeners(device);
        callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE);

        callKnownSynchronizedDeviceConnected(device);
      }
    }
  }

  protected boolean addDeviceToKnownSynchronizedDevices(DiscoveredDevice device) {
    String deviceInfo = getDeviceInfoFromDevice(device.getDevice());

    if(deviceInfo != null) {
      unknownDevices.remove(deviceInfo);
      knownIgnoredDevices.remove(deviceInfo);

      knownSynchronizedDevices.put(deviceInfo, device);

      return true;
    }

    return false;
  }

  protected void addDeviceToLocalConfigSynchronizedDevices(DiscoveredDevice device) {
    if(localConfig.getIgnoredDevices().contains(device.getDevice())) {
      localConfig.removeIgnoredDevice(device.getDevice());
    }
    localConfig.addSynchronizedDevice(device.getDevice());

    entityManager.updateEntity(localConfig);
  }

  protected void setSourceAndDestinationSyncConfigurationOnDevices(DiscoveredDevice device, SyncConfiguration syncConfiguration) {
    Device localDevice = localConfig.getLocalDevice();
    localDevice.addSourceSyncConfiguration(syncConfiguration);
    entityManager.updateEntity(localDevice);

    Device remoteDevice = device.getDevice();
    remoteDevice.addDestinationSyncConfiguration(syncConfiguration);
    entityManager.updateEntity(remoteDevice);
  }


  @Override
  public void stopSynchronizingWithDevice(DiscoveredDevice device) {
    if(localConfig.removeSynchronizedDevice(device.getDevice())) {
      if(entityManager.updateEntity(localConfig)) {
        deleteSyncConfigurationForStoppedSynchronizedDevice(device);

        String deviceInfo = getDeviceInfoFromDevice(device.getDevice());
        knownSynchronizedDevices.remove(deviceInfo);
        unknownDevices.put(deviceInfo, device);

        removeFromSourceAndDestinationSyncConfigurationOnDevices(device.getDevice());

        callKnownSynchronizedDeviceDisconnected(device);

        callDiscoveredDeviceDisconnectedListeners(device);
        callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.UNKNOWN_DEVICE);
      }
    }
  }

  protected void removeFromSourceAndDestinationSyncConfigurationOnDevices(Device remoteDevice) {
    Device localDevice = localConfig.getLocalDevice();

    for(SyncConfiguration syncConfiguration : localDevice.getSourceSyncConfigurations()) {
      if(syncConfiguration.getDestinationDevice() == remoteDevice) {
        localDevice.removeSourceSyncConfiguration(syncConfiguration);
        remoteDevice.removeDestinationSyncConfiguration(syncConfiguration);

        break;
      }
    }
  }

  protected boolean deleteSyncConfigurationForStoppedSynchronizedDevice(DiscoveredDevice device) {
    SyncConfiguration deviceSyncConfiguration = null;

    for(SyncConfiguration syncConfig : entityManager.getAllEntitiesOfType(SyncConfiguration.class)) {
      if(device.getDevice() == syncConfig.getDestinationDevice()) {
        deviceSyncConfiguration = syncConfig;
        break;
      }
    }

    if(deviceSyncConfiguration != null) {
      for(SyncModuleConfiguration syncModuleConfig : new ArrayList<>(deviceSyncConfiguration.getSyncModuleConfigurations())) {
        entityManager.deleteEntity(syncModuleConfig);
      }

      return entityManager.deleteEntity(deviceSyncConfiguration);
    }

    return false;
  }

  @Override
  public void addDeviceToIgnoreList(DiscoveredDevice device) {
    if(localConfig.addIgnoredDevice(device.getDevice())) {
      if(entityManager.updateEntity(localConfig)) {
        String deviceInfo = getDeviceInfoFromDevice(device.getDevice());
        unknownDevices.remove(deviceInfo);
        knownIgnoredDevices.put(deviceInfo, device);

        callDiscoveredDeviceDisconnectedListeners(device);
        callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_IGNORED_DEVICE);
      }
    }
  }

  @Override
  public void startSynchronizingWithIgnoredDevice(DiscoveredDevice device, List<SyncModuleConfiguration> syncModuleConfigurations) {
    if(localConfig.removeIgnoredDevice(device.getDevice())) {
        if(entityManager.updateEntity(localConfig)) {
          String deviceInfo = getDeviceInfoFromDevice(device.getDevice());
          knownIgnoredDevices.remove(deviceInfo);

          startSynchronizingWithDevice(device, syncModuleConfigurations);
        }
    }
  }


  @Override
  public boolean addDiscoveredDevicesListener(DiscoveredDevicesListener listener) {
    return discoveredDevicesListeners.add(listener);
  }

  @Override
  public boolean removeDiscoveredDevicesListener(DiscoveredDevicesListener listener) {
    return discoveredDevicesListeners.remove(listener);
  }

  protected void callDiscoveredDeviceConnectedListeners(DiscoveredDevice device, DiscoveredDeviceType type) {
    for(DiscoveredDevicesListener listener : discoveredDevicesListeners) {
      listener.deviceDiscovered(device, type);
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


  @Override
  public List<DiscoveredDevice> getAllDiscoveredDevices() {
    return new ArrayList<>(discoveredDevices.values());
  }

  @Override
  public List<DiscoveredDevice> getKnownSynchronizedDiscoveredDevices() {
    return new ArrayList<>(knownSynchronizedDevices.values());
  }

  @Override
  public List<DiscoveredDevice> getKnownIgnoredDiscoveredDevices() {
    return new ArrayList<>(knownIgnoredDevices.values());
  }

  @Override
  public List<DiscoveredDevice> getUnknownDiscoveredDevices() {
    return new ArrayList<>(unknownDevices.values());
  }

}
