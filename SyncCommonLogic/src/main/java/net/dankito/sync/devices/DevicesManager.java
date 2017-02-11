package net.dankito.sync.devices;

import net.dankito.devicediscovery.DevicesDiscovererConfig;
import net.dankito.devicediscovery.DevicesDiscovererListener;
import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.communication.IClientCommunicator;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.RequestStartSynchronizationResponseBody;
import net.dankito.sync.communication.message.RequestStartSynchronizationResult;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.persistence.IEntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Named;


@Named
public class DevicesManager implements IDevicesManager {

  protected static final String DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR = ":";

  private static final Logger log = LoggerFactory.getLogger(DevicesManager.class);


  protected IDevicesDiscoverer devicesDiscoverer;

  protected IClientCommunicator clientCommunicator;

  protected LocalConfig localConfig;

  protected INetworkSettings networkSettings;

  protected IEntityManager entityManager;


  protected Map<String, DiscoveredDevice> discoveredDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> devicesPendingStartSynchronization = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> knownSynchronizedDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> knownIgnoredDevices = new ConcurrentHashMap<>();

  protected Map<String, DiscoveredDevice> unknownDevices = new ConcurrentHashMap<>();


  protected List<DiscoveredDevicesListener> discoveredDevicesListeners = new CopyOnWriteArrayList<>();

  protected List<KnownSynchronizedDevicesListener> knownSynchronizedDevicesListeners = new CopyOnWriteArrayList<>();


  public DevicesManager(IDevicesDiscoverer devicesDiscoverer, IClientCommunicator clientCommunicator, IDataManager dataManager, INetworkSettings networkSettings, IEntityManager entityManager) {
    this.devicesDiscoverer = devicesDiscoverer;
    this.clientCommunicator = clientCommunicator;
    this.networkSettings = networkSettings;
    this.entityManager = entityManager;

    this.localConfig = dataManager.getLocalConfig();
  }


  @Override
  public void start() {
    String localDeviceInfoKey = getDeviceInfoKey(networkSettings);

    devicesDiscoverer.startAsync(new DevicesDiscovererConfig(localDeviceInfoKey, DevicesManagerConfig.DEVICES_DISCOVERER_PORT,
        DevicesManagerConfig.CHECK_FOR_DEVICES_INTERVAL_MILLIS, DevicesManagerConfig.DISCOVERY_MESSAGE_PREFIX, discovererListener));
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


  protected DevicesDiscovererListener discovererListener = new DevicesDiscovererListener() {
    @Override
    public void deviceFound(String deviceInfo, String address) {
      getDeviceDetailsForDiscoveredDevice(deviceInfo, address);
    }

    @Override
    public void deviceDisconnected(String deviceInfo) {
      DevicesManager.this.deviceDisconnected(deviceInfo);
    }
  };

  protected void getDeviceDetailsForDiscoveredDevice(String deviceInfoKey, final String address) {
    try {
      final int messagesPort = getMessagesPortFromDeviceInfoKey(deviceInfoKey);

      retrieveDeviceInfoFromRemote(deviceInfoKey, address, messagesPort);
    } catch(Exception e) {
      log.error("Could not deserialize Device from " + deviceInfoKey, e);
    }
  }

  protected void retrieveDeviceInfoFromRemote(final String deviceInfoKey, final String address, final int messagesPort) {
    clientCommunicator.getDeviceInfo(new InetSocketAddress(address, messagesPort), new SendRequestCallback<DeviceInfo>() {
      @Override
      public void done(Response<DeviceInfo> response) {
        if(response.isCouldHandleMessage()) {
          successfullyRetrievedDeviceInfo(deviceInfoKey, response.getBody(), address, messagesPort);
        }
        else {
          // TODO: try periodically to get DeviceInfo from remote
        }
      }
    });
  }

  protected void successfullyRetrievedDeviceInfo(String deviceInfoKey, DeviceInfo deviceInfo, String address, int messagesPort) {
    Device remoteDevice = getPersistedDeviceForUniqueId(deviceInfo.getUniqueDeviceId());

    if(remoteDevice == null) { // remote device not known and therefore persisted yet
      remoteDevice = mapDeviceInfoToDevice(deviceInfo);

      entityManager.persistEntity(remoteDevice);
    }

    discoveredDevice(deviceInfoKey, remoteDevice, address, messagesPort);
  }

  protected Device getPersistedDeviceForUniqueId(String deviceUniqueId) {
    List<Device> persistedDevices = entityManager.getAllEntitiesOfType(Device.class);
    for(Device device : persistedDevices) {
      if(deviceUniqueId.equals(device.getUniqueDeviceId())) {
        return device;
      }
    }

    return null;
  }

  protected Device mapDeviceInfoToDevice(DeviceInfo deviceInfo) {
    return new Device(deviceInfo.getId(), deviceInfo.getUniqueDeviceId(), deviceInfo.getName(), deviceInfo.getOsType(), deviceInfo.getOsName(),
        deviceInfo.getOsVersion(), deviceInfo.getDescription());
  }

  protected void discoveredDevice(String deviceInfoKey, Device device, String address, int messagesPort) {
    DiscoveredDevice discoveredDevice = new DiscoveredDevice(device, address);

    discoveredDevice.setMessagesPort(messagesPort);

    discoveredDevice(deviceInfoKey, discoveredDevice);
  }

  protected void discoveredDevice(String deviceInfoKey, DiscoveredDevice device) {
    synchronized(discoveredDevices) {
      discoveredDevices.put(deviceInfoKey, device);

      DiscoveredDeviceType type = determineDiscoveredDeviceType(device);

      if(type == DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
        discoveredKnownSynchronizedDevice(device, deviceInfoKey);
        knownSynchronizedDevices.put(deviceInfoKey, device);
      }
      else if(type == DiscoveredDeviceType.KNOWN_IGNORED_DEVICE) {
        knownIgnoredDevices.put(deviceInfoKey, device);
      }
      else {
        unknownDevices.put(deviceInfoKey, device);
      }

      callDiscoveredDeviceConnectedListeners(device, type);
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

  protected void discoveredKnownSynchronizedDevice(final DiscoveredDevice device, final String deviceInfoKey) {
    devicesPendingStartSynchronization.put(deviceInfoKey, device);

    clientCommunicator.requestStartSynchronization(device, new SendRequestCallback<RequestStartSynchronizationResponseBody>() {
      @Override
      public void done(Response<RequestStartSynchronizationResponseBody> response) {
        handleRequestStartSynchronizationResponse(response, device, deviceInfoKey);
      }
    });
  }

  protected void handleRequestStartSynchronizationResponse(Response<RequestStartSynchronizationResponseBody> response, DiscoveredDevice device, String deviceInfoKey) {
    if(response.isCouldHandleMessage()) {
      RequestStartSynchronizationResponseBody body = response.getBody();

      if(body.getResult() == RequestStartSynchronizationResult.ALLOWED) {
        device.setSynchronizationPort(body.getSynchronizationPort());

        devicesPendingStartSynchronization.remove(deviceInfoKey);

        callKnownSynchronizedDeviceConnected(device);
      }
    }
    // TODO: what to do if not?
  }


  protected void deviceDisconnected(String deviceInfoKey) {
    DiscoveredDevice device = discoveredDevices.get(deviceInfoKey);
    if(device != null) {
      disconnectedFromDevice(deviceInfoKey, device);
    }
    else {
      log.error("This should never occur! Disconnected from Device, but was not in discoveredDevices: " + deviceInfoKey);
    }
  }

  protected void disconnectedFromDevice(String deviceInfo, DiscoveredDevice device) {
    synchronized(discoveredDevices) {
      discoveredDevices.remove(deviceInfo);

      if(isKnownSynchronizedDevice(device)) {
        knownSynchronizedDevices.remove(deviceInfo);
        networkSettings.removeConnectedDevicePermittedToSynchronize(device.getDevice());
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


  protected String getDeviceInfoKey(INetworkSettings networkSettings) {
    DiscoveredDevice localDevice = new DiscoveredDevice(networkSettings.getLocalHostDevice(), "localhost");
    localDevice.setMessagesPort(networkSettings.getMessagePort());

    return getDeviceInfoKey(localDevice);
  }

  protected String getDeviceInfoKey(DiscoveredDevice device) {
    return device.getDevice().getUniqueDeviceId() + DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR + device.getMessagesPort();
  }

  protected String getDeviceUniqueIdFromDeviceInfoKey(String deviceInfoKey) {
    int portStartIndex = deviceInfoKey.lastIndexOf(DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR);
    if(portStartIndex > 0) {
      return deviceInfoKey.substring(0, portStartIndex);
    }

    return null;
  }

  protected int getMessagesPortFromDeviceInfoKey(String deviceInfoKey) {
    int portStartIndex = deviceInfoKey.lastIndexOf(DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR);
    if(portStartIndex > 0) {
      portStartIndex += DEVICE_ID_AND_MESSAGES_PORT_SEPARATOR.length();

      String portString = deviceInfoKey.substring(portStartIndex);
      return Integer.parseInt(portString);
    }

    return -1;
  }


  @Override
  public void startSynchronizingWithDevice(DiscoveredDevice device, List<SyncModuleConfiguration> syncModuleConfigurations) {
    // TODO: the whole process should actually run in a transaction
    SyncConfiguration syncConfiguration = new SyncConfiguration(localConfig.getLocalDevice(), device.getDevice(), syncModuleConfigurations);
    if(entityManager.persistEntity(syncConfiguration)) {
      addDeviceToKnownSynchronizedDevicesAndCallListeners(device, syncConfiguration);
    }
  }

  @Override
  public void remoteDeviceStartedSynchronizingWithUs(Device remoteDevice) {
    DiscoveredDevice discoveredRemoteDevice = getDiscoveredDeviceForDevice(remoteDevice);

    for(DiscoveredDevice discoveredDevice : discoveredDevices.values()) {
      if(discoveredDevice.equals(discoveredRemoteDevice)) {
        addDeviceToKnownSynchronizedDevicesAndCallListeners(discoveredDevice, null);
        break;
      }
    }
  }

  protected void addDeviceToKnownSynchronizedDevicesAndCallListeners(DiscoveredDevice device, SyncConfiguration syncConfiguration) {
    if(addDeviceToKnownSynchronizedDevices(device)) {
      addDeviceToLocalConfigSynchronizedDevices(device);

      if(syncConfiguration != null) { // if remote device started synchronization, syncConfiguration is null
        setSourceAndDestinationSyncConfigurationOnDevices(device, syncConfiguration);
      }

      callDiscoveredDeviceDisconnectedListeners(device);
      callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE);

      callKnownSynchronizedDeviceConnected(device);
    }
  }

  protected boolean addDeviceToKnownSynchronizedDevices(DiscoveredDevice device) {
    String deviceInfoKey = getDeviceInfoKey(device);

    if(deviceInfoKey != null) {
      unknownDevices.remove(deviceInfoKey);
      knownIgnoredDevices.remove(deviceInfoKey);

      knownSynchronizedDevices.put(deviceInfoKey, device);

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
    localConfig.removeSynchronizedDevice(device.getDevice());

    String deviceInfoKey = getDeviceInfoKey(device);
    knownSynchronizedDevices.remove(deviceInfoKey);
    unknownDevices.put(deviceInfoKey, device);

    removeFromSourceAndDestinationSyncConfigurationOnDevices(device.getDevice());

    deleteSyncConfigurationForStoppedSynchronizedDevice(device);

    entityManager.updateEntity(localConfig);

    callKnownSynchronizedDeviceDisconnected(device);

    callDiscoveredDeviceDisconnectedListeners(device);
    callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.UNKNOWN_DEVICE);
  }

  protected void removeFromSourceAndDestinationSyncConfigurationOnDevices(Device remoteDevice) {
    Device localDevice = localConfig.getLocalDevice();

    for(SyncConfiguration syncConfiguration : localDevice.getSourceSyncConfigurations()) {
      if(syncConfiguration.getDestinationDevice() == remoteDevice) {
        localDevice.removeSourceSyncConfiguration(syncConfiguration);
        remoteDevice.removeDestinationSyncConfiguration(syncConfiguration);

        entityManager.updateEntity(localDevice);
        entityManager.updateEntity(remoteDevice);

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
        String deviceInfoKey = getDeviceInfoKey(device);
        unknownDevices.remove(deviceInfoKey);
        knownIgnoredDevices.put(deviceInfoKey, device);

        callDiscoveredDeviceDisconnectedListeners(device);
        callDiscoveredDeviceConnectedListeners(device, DiscoveredDeviceType.KNOWN_IGNORED_DEVICE);
      }
    }
  }

  @Override
  public void startSynchronizingWithIgnoredDevice(DiscoveredDevice device, List<SyncModuleConfiguration> syncModuleConfigurations) {
    if(localConfig.removeIgnoredDevice(device.getDevice())) {
        if(entityManager.updateEntity(localConfig)) {
          String deviceInfoKey = getDeviceInfoKey(device);
          knownIgnoredDevices.remove(deviceInfoKey);

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


  public DiscoveredDevice getDiscoveredDeviceForDevice(Device device) {
    return getDiscoveredDeviceForId(device.getUniqueDeviceId());
  }

  public DiscoveredDevice getDiscoveredDeviceForId(String uniqueDeviceId) {
    for(DiscoveredDevice device : getAllDiscoveredDevices()) {
      if(device.getDevice().getUniqueDeviceId().equals(uniqueDeviceId)) {
        return device;
      }
    }

    return null;
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
