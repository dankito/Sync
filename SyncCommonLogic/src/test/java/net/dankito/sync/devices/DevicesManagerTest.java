package net.dankito.sync.devices;


import net.dankito.devicediscovery.DevicesDiscovererConfig;
import net.dankito.devicediscovery.DevicesDiscovererListener;
import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.OsType;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.communication.IClientCommunicator;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.communication.message.RequestStartSynchronizationResponseBody;
import net.dankito.sync.communication.message.RequestStartSynchronizationResult;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DevicesManagerTest {

  protected static final int MESSAGES_RECEIVER_PORT = 54321;

  protected static final String REMOTE_DEVICE_ID = "1";

  protected static final String REMOTE_DEVICE_UNIQUE_DEVICE_ID = "1";

  protected static final String REMOTE_DEVICE_NAME = "remote";

  protected static final String REMOTE_DEVICE_OS_NAME = "testOs";

  protected static final String REMOTE_DEVICE_OS_VERSION = "0.0.1-alpha";

  protected static final OsType REMOTE_DEVICE_OS_TYPE = OsType.DESKTOP;

  protected static final String REMOTE_DEVICE_DESCRIPTION = "desc";

  protected static final String REMOTE_DEVICE_ADDRESS = "192.168.254.254";

  protected static final int REMOTE_DEVICE_MESSAGES_PORT = 47;

  protected static final int REMOTE_DEVICE_SYNCHRONIZATION_PORT = 48;


  protected DevicesManager underTest;

  protected IEntityManager entityManager;

  protected IDataManager dataManager;

  protected IClientCommunicator clientCommunicator;

  protected IDevicesDiscoverer devicesDiscoverer;

  protected LocalConfig localConfig;

  protected Device remoteDevice;

  protected DevicesDiscovererListener devicesDiscovererListener;


  @Before
  public void setUp() throws Exception {
    entityManager = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData", 1));

    Device localDevice = new Device("Local");
    this.localConfig = new LocalConfig(localDevice);
    entityManager.persistEntity(localConfig);

    remoteDevice = new Device(REMOTE_DEVICE_ID, REMOTE_DEVICE_UNIQUE_DEVICE_ID, REMOTE_DEVICE_NAME, REMOTE_DEVICE_OS_TYPE, REMOTE_DEVICE_OS_NAME, REMOTE_DEVICE_OS_VERSION, REMOTE_DEVICE_DESCRIPTION);

    final INetworkSettings networkSettings = new NetworkSettings(localConfig);

    dataManager = mock(IDataManager.class);
    when(dataManager.getLocalConfig()).thenReturn(localConfig);

    clientCommunicator = mock(IClientCommunicator.class);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        SendRequestCallback callback = (SendRequestCallback)invocation.getArguments()[1];
        callback.done(new Response(DeviceInfo.fromDevice(remoteDevice)));
        return null;
      }
    }).when(clientCommunicator).getDeviceInfo(any(SocketAddress.class), any(SendRequestCallback.class));

    devicesDiscoverer = mock(IDevicesDiscoverer.class);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        DevicesDiscovererConfig config = (DevicesDiscovererConfig)invocation.getArguments()[0];
        devicesDiscovererListener = config.getListener();
        return null;
      }
    }).when(devicesDiscoverer).startAsync(any(DevicesDiscovererConfig.class));

    underTest = new DevicesManager(devicesDiscoverer, clientCommunicator, dataManager, networkSettings, entityManager);
  }

  @After
  public void tearDown() {
    underTest.stop();

    clientCommunicator.stop();

    new JavaFileStorageService().deleteFolderRecursively(entityManager.getDatabasePath());
  }


  @Test
  public void startSynchronizingWithDevice() {
    DiscoveredDevice discoveredDevice = mockUnknownDiscoveredDevice();

    Assert.assertFalse(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(1, underTest.unknownDevices.size());
    Assert.assertEquals(0, underTest.knownSynchronizedDevices.size());

    Assert.assertEquals(0, localConfig.getLocalDevice().getSourceSyncConfigurations().size());
    Assert.assertEquals(0, discoveredDevice.getDevice().getDestinationSyncConfigurations().size());


    underTest.startSynchronizingWithDevice(discoveredDevice, new ArrayList<SyncModuleConfiguration>());


    Assert.assertTrue(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    List<SyncConfiguration> syncConfigurations = entityManager.getAllEntitiesOfType(SyncConfiguration.class);
    Assert.assertEquals(1, syncConfigurations.size());

    Assert.assertEquals(0, underTest.unknownDevices.size());
    Assert.assertEquals(1, underTest.knownSynchronizedDevices.size());

    Assert.assertEquals(1, localConfig.getLocalDevice().getSourceSyncConfigurations().size());
    Assert.assertEquals(1, discoveredDevice.getDevice().getDestinationSyncConfigurations().size());
  }


  @Test
  public void stopSynchronizingWithDevice() {
    DiscoveredDevice discoveredDevice = mockUnknownDiscoveredDevice();

    underTest.startSynchronizingWithDevice(discoveredDevice, new ArrayList<SyncModuleConfiguration>());

    Assert.assertTrue(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(0, underTest.unknownDevices.size());
    Assert.assertEquals(1, underTest.knownSynchronizedDevices.size());

    Assert.assertEquals(1, localConfig.getLocalDevice().getSourceSyncConfigurations().size());
    Assert.assertEquals(1, discoveredDevice.getDevice().getDestinationSyncConfigurations().size());


    underTest.stopSynchronizingWithDevice(discoveredDevice);


    Assert.assertFalse(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(1, underTest.unknownDevices.size());
    Assert.assertEquals(0, underTest.knownSynchronizedDevices.size());

    Assert.assertEquals(0, localConfig.getLocalDevice().getSourceSyncConfigurations().size());
    Assert.assertEquals(0, discoveredDevice.getDevice().getDestinationSyncConfigurations().size());
  }


  @Test
  public void addDeviceToIgnoreList() {
    DiscoveredDevice discoveredDevice = mockUnknownDiscoveredDevice();

    Assert.assertFalse(localConfig.getIgnoredDevices().contains(discoveredDevice.getDevice()));

    Assert.assertEquals(1, underTest.unknownDevices.size());
    Assert.assertEquals(0, underTest.knownIgnoredDevices.size());


    underTest.addDeviceToIgnoreList(discoveredDevice);


    Assert.assertTrue(localConfig.getIgnoredDevices().contains(discoveredDevice.getDevice()));

    Assert.assertEquals(0, underTest.unknownDevices.size());
    Assert.assertEquals(1, underTest.knownIgnoredDevices.size());
  }


  @Test
  public void startSynchronizingWithIgnoredDevice() {
    DiscoveredDevice discoveredDevice = mockIgnoredDiscoveredDevice();

    Assert.assertTrue(localConfig.getIgnoredDevices().contains(discoveredDevice.getDevice()));
    Assert.assertFalse(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));

    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(1, underTest.knownIgnoredDevices.size());
    Assert.assertEquals(0, underTest.knownSynchronizedDevices.size());

    Assert.assertEquals(0, localConfig.getLocalDevice().getSourceSyncConfigurations().size());
    Assert.assertEquals(0, discoveredDevice.getDevice().getDestinationSyncConfigurations().size());


    underTest.startSynchronizingWithIgnoredDevice(discoveredDevice, new ArrayList<SyncModuleConfiguration>());


    Assert.assertFalse(localConfig.getIgnoredDevices().contains(discoveredDevice.getDevice()));
    Assert.assertTrue(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));

    List<SyncConfiguration> syncConfigurations = entityManager.getAllEntitiesOfType(SyncConfiguration.class);
    Assert.assertEquals(1, syncConfigurations.size());

    Assert.assertEquals(0, underTest.knownIgnoredDevices.size());
    Assert.assertEquals(1, underTest.knownSynchronizedDevices.size());

    Assert.assertEquals(1, localConfig.getLocalDevice().getSourceSyncConfigurations().size());
    Assert.assertEquals(1, discoveredDevice.getDevice().getDestinationSyncConfigurations().size());
  }


  @Test
  public void devicesDiscovererFindsUnknownDevice_ListenerGetsCalled() {
    assertThat(underTest.getAllDiscoveredDevices().size(), is(0));
    assertThat(underTest.getUnknownDiscoveredDevices().size(), is(0));

    underTest.start();

    final ObjectHolder<DiscoveredDevice> discoveredDeviceHolder = new ObjectHolder<>();
    final ObjectHolder<DiscoveredDeviceType> discoveredDeviceTypeHolder = new ObjectHolder<>();
    final AtomicInteger countDeviceDiscoveredCalled = new AtomicInteger(0);
    final AtomicInteger countDisconnectedFromDeviceCalled = new AtomicInteger(0);

    underTest.addDiscoveredDevicesListener(new DiscoveredDevicesListener() {
      @Override
      public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
        discoveredDeviceHolder.setObject(connectedDevice);
        discoveredDeviceTypeHolder.setObject(type);
        countDeviceDiscoveredCalled.incrementAndGet();
      }

      @Override
      public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
      countDisconnectedFromDeviceCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceFound(underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS)), REMOTE_DEVICE_ADDRESS);


    assertThat(underTest.getAllDiscoveredDevices().size(), is(1));
    assertThat(underTest.getUnknownDiscoveredDevices().size(), is(1));

    assertThat(countDeviceDiscoveredCalled.get(), is(1));
    assertThat(countDisconnectedFromDeviceCalled.get(), is(0));

    assertThat(discoveredDeviceTypeHolder.isObjectSet(), is(true));
    assertThat(discoveredDeviceTypeHolder.getObject(), is(DiscoveredDeviceType.UNKNOWN_DEVICE));

    assertThat(discoveredDeviceHolder.isObjectSet(), is(true));
    assertDiscoveredDeviceHasCorrectlyBeenSet(discoveredDeviceHolder.getObject());
  }


  @Test
  public void devicesDiscovererFindsKnownSynchronizedDevice_ListenerGetsCalled() {
    assertThat(underTest.getAllDiscoveredDevices().size(), is(0));
    assertThat(underTest.getKnownSynchronizedDiscoveredDevices().size(), is(0));

    entityManager.persistEntity(remoteDevice);
    localConfig.addSynchronizedDevice(remoteDevice);

    underTest.start();

    final ObjectHolder<DiscoveredDevice> discoveredDeviceHolder = new ObjectHolder<>();
    final ObjectHolder<DiscoveredDeviceType> discoveredDeviceTypeHolder = new ObjectHolder<>();
    final AtomicInteger countDeviceDiscoveredCalled = new AtomicInteger(0);
    final AtomicInteger countDisconnectedFromDeviceCalled = new AtomicInteger(0);

    underTest.addDiscoveredDevicesListener(new DiscoveredDevicesListener() {
      @Override
      public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
        discoveredDeviceHolder.setObject(connectedDevice);
        discoveredDeviceTypeHolder.setObject(type);
        countDeviceDiscoveredCalled.incrementAndGet();
      }

      @Override
      public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
        countDisconnectedFromDeviceCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceFound(underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS)), REMOTE_DEVICE_ADDRESS);


    assertThat(underTest.getAllDiscoveredDevices().size(), is(1));
    assertThat(underTest.getKnownSynchronizedDiscoveredDevices().size(), is(1));

    assertThat(countDeviceDiscoveredCalled.get(), is(1));
    assertThat(countDisconnectedFromDeviceCalled.get(), is(0));

    assertThat(discoveredDeviceTypeHolder.isObjectSet(), is(true));
    assertThat(discoveredDeviceTypeHolder.getObject(), is(DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE));

    assertThat(discoveredDeviceHolder.isObjectSet(), is(true));
    assertDiscoveredDeviceHasCorrectlyBeenSet(discoveredDeviceHolder.getObject());
  }


  @Test
  public void devicesDiscovererFindsKnownIgnoredDevice_ListenerGetsCalled() {
    assertThat(underTest.getAllDiscoveredDevices().size(), is(0));
    assertThat(underTest.getKnownIgnoredDiscoveredDevices().size(), is(0));

    entityManager.persistEntity(remoteDevice);
    localConfig.addIgnoredDevice(remoteDevice);

    underTest.start();

    final ObjectHolder<DiscoveredDevice> discoveredDeviceHolder = new ObjectHolder<>();
    final ObjectHolder<DiscoveredDeviceType> discoveredDeviceTypeHolder = new ObjectHolder<>();
    final AtomicInteger countDeviceDiscoveredCalled = new AtomicInteger(0);
    final AtomicInteger countDisconnectedFromDeviceCalled = new AtomicInteger(0);

    underTest.addDiscoveredDevicesListener(new DiscoveredDevicesListener() {
      @Override
      public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
        discoveredDeviceHolder.setObject(connectedDevice);
        discoveredDeviceTypeHolder.setObject(type);
        countDeviceDiscoveredCalled.incrementAndGet();
      }

      @Override
      public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
        countDisconnectedFromDeviceCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceFound(underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS)), REMOTE_DEVICE_ADDRESS);


    assertThat(underTest.getAllDiscoveredDevices().size(), is(1));
    assertThat(underTest.getKnownIgnoredDiscoveredDevices().size(), is(1));

    assertThat(countDeviceDiscoveredCalled.get(), is(1));
    assertThat(countDisconnectedFromDeviceCalled.get(), is(0));

    assertThat(discoveredDeviceTypeHolder.isObjectSet(), is(true));
    assertThat(discoveredDeviceTypeHolder.getObject(), is(DiscoveredDeviceType.KNOWN_IGNORED_DEVICE));

    assertThat(discoveredDeviceHolder.isObjectSet(), is(true));
    assertDiscoveredDeviceHasCorrectlyBeenSet(discoveredDeviceHolder.getObject());
  }


  @Test
  public void disconnectedFromUnknownDevice_ListenerGetsCalled() {
    underTest.start();

    String deviceInfoKey = underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS));
    devicesDiscovererListener.deviceFound(deviceInfoKey, REMOTE_DEVICE_ADDRESS);

    assertThat(underTest.getAllDiscoveredDevices().size(), is(1));
    assertThat(underTest.getUnknownDiscoveredDevices().size(), is(1));

    final ObjectHolder<DiscoveredDevice> disconnectedDeviceHolder = new ObjectHolder<>();
    final AtomicInteger countDeviceDiscoveredCalled = new AtomicInteger(0);
    final AtomicInteger countDisconnectedFromDeviceCalled = new AtomicInteger(0);

    underTest.addDiscoveredDevicesListener(new DiscoveredDevicesListener() {
      @Override
      public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
        countDeviceDiscoveredCalled.incrementAndGet();
      }

      @Override
      public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
        disconnectedDeviceHolder.setObject(disconnectedDevice);
        countDisconnectedFromDeviceCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceDisconnected(deviceInfoKey);


    assertThat(underTest.getAllDiscoveredDevices().size(), is(0));
    assertThat(underTest.getUnknownDiscoveredDevices().size(), is(0));

    assertThat(countDeviceDiscoveredCalled.get(), is(0));
    assertThat(countDisconnectedFromDeviceCalled.get(), is(1));

    assertThat(disconnectedDeviceHolder.isObjectSet(), is(true));
    assertDiscoveredDeviceHasCorrectlyBeenSet(disconnectedDeviceHolder.getObject());
  }

  @Test
  public void disconnectedFromKnownSynchronizedDevice_ListenerGetsCalled() {
    entityManager.persistEntity(remoteDevice);
    localConfig.addSynchronizedDevice(remoteDevice);

    underTest.start();

    String deviceInfoKey = underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS));
    devicesDiscovererListener.deviceFound(deviceInfoKey, REMOTE_DEVICE_ADDRESS);

    assertThat(underTest.getAllDiscoveredDevices().size(), is(1));
    assertThat(underTest.getKnownSynchronizedDiscoveredDevices().size(), is(1));

    final ObjectHolder<DiscoveredDevice> disconnectedDeviceHolder = new ObjectHolder<>();
    final AtomicInteger countDeviceDiscoveredCalled = new AtomicInteger(0);
    final AtomicInteger countDisconnectedFromDeviceCalled = new AtomicInteger(0);

    underTest.addDiscoveredDevicesListener(new DiscoveredDevicesListener() {
      @Override
      public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
        countDeviceDiscoveredCalled.incrementAndGet();
      }

      @Override
      public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
        disconnectedDeviceHolder.setObject(disconnectedDevice);
        countDisconnectedFromDeviceCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceDisconnected(deviceInfoKey);


    assertThat(underTest.getAllDiscoveredDevices().size(), is(0));
    assertThat(underTest.getKnownSynchronizedDiscoveredDevices().size(), is(0));

    assertThat(countDeviceDiscoveredCalled.get(), is(0));
    assertThat(countDisconnectedFromDeviceCalled.get(), is(1));

    assertThat(disconnectedDeviceHolder.isObjectSet(), is(true));
    assertDiscoveredDeviceHasCorrectlyBeenSet(disconnectedDeviceHolder.getObject());
  }

  @Test
  public void disconnectedFromKnownIgnoredDevice_ListenerGetsCalled() {
    entityManager.persistEntity(remoteDevice);
    localConfig.addIgnoredDevice(remoteDevice);

    underTest.start();

    String deviceInfoKey = underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS));
    devicesDiscovererListener.deviceFound(deviceInfoKey, REMOTE_DEVICE_ADDRESS);

    assertThat(underTest.getAllDiscoveredDevices().size(), is(1));
    assertThat(underTest.getKnownIgnoredDiscoveredDevices().size(), is(1));

    final ObjectHolder<DiscoveredDevice> disconnectedDeviceHolder = new ObjectHolder<>();
    final AtomicInteger countDeviceDiscoveredCalled = new AtomicInteger(0);
    final AtomicInteger countDisconnectedFromDeviceCalled = new AtomicInteger(0);

    underTest.addDiscoveredDevicesListener(new DiscoveredDevicesListener() {
      @Override
      public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
        countDeviceDiscoveredCalled.incrementAndGet();
      }

      @Override
      public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
        disconnectedDeviceHolder.setObject(disconnectedDevice);
        countDisconnectedFromDeviceCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceDisconnected(deviceInfoKey);


    assertThat(underTest.getAllDiscoveredDevices().size(), is(0));
    assertThat(underTest.getKnownIgnoredDiscoveredDevices().size(), is(0));

    assertThat(countDeviceDiscoveredCalled.get(), is(0));
    assertThat(countDisconnectedFromDeviceCalled.get(), is(1));

    assertThat(disconnectedDeviceHolder.isObjectSet(), is(true));
    assertDiscoveredDeviceHasCorrectlyBeenSet(disconnectedDeviceHolder.getObject());
  }


  @Test
  public void requestStartSynchronizationReturnsAllowed_KnownSynchronizedDevicesListenerGetsCalled() {
    entityManager.persistEntity(remoteDevice);
    localConfig.addSynchronizedDevice(remoteDevice);

    mockRequestStartSynchronization(RequestStartSynchronizationResult.ALLOWED, REMOTE_DEVICE_SYNCHRONIZATION_PORT);

    underTest.start();

    final ObjectHolder<DiscoveredDevice> synchronizedDeviceHolder = new ObjectHolder<>();
    final AtomicInteger countKnownSynchronizedDeviceConnectedCalled = new AtomicInteger(0);
    final AtomicInteger countKnownSynchronizedDeviceDisconnectedCalled = new AtomicInteger(0);

    underTest.addKnownSynchronizedDevicesListener(new KnownSynchronizedDevicesListener() {
      @Override
      public void knownSynchronizedDeviceConnected(DiscoveredDevice connectedDevice) {
        synchronizedDeviceHolder.setObject(connectedDevice);
        countKnownSynchronizedDeviceConnectedCalled.incrementAndGet();
      }

      @Override
      public void knownSynchronizedDeviceDisconnected(DiscoveredDevice disconnectedDevice) {
        countKnownSynchronizedDeviceDisconnectedCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceFound(underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS)), REMOTE_DEVICE_ADDRESS);


    assertThat(countKnownSynchronizedDeviceConnectedCalled.get(), is(1));
    assertThat(countKnownSynchronizedDeviceDisconnectedCalled.get(), is(0));

    assertThat(synchronizedDeviceHolder.isObjectSet(), is(true));
    assertThat(synchronizedDeviceHolder.getObject().getSynchronizationPort(), is(REMOTE_DEVICE_SYNCHRONIZATION_PORT));
    assertDiscoveredDeviceHasCorrectlyBeenSet(synchronizedDeviceHolder.getObject());
  }

  @Test
  public void requestStartSynchronizationReturnsDenied_KnownSynchronizedDevicesListenerDoesNotGetCalled() {
    entityManager.persistEntity(remoteDevice);
    localConfig.addSynchronizedDevice(remoteDevice);

    mockRequestStartSynchronization(RequestStartSynchronizationResult.DENIED);

    underTest.start();

    final ObjectHolder<DiscoveredDevice> synchronizedDeviceHolder = new ObjectHolder<>();
    final AtomicInteger countKnownSynchronizedDeviceConnectedCalled = new AtomicInteger(0);
    final AtomicInteger countKnownSynchronizedDeviceDisconnectedCalled = new AtomicInteger(0);

    underTest.addKnownSynchronizedDevicesListener(new KnownSynchronizedDevicesListener() {
      @Override
      public void knownSynchronizedDeviceConnected(DiscoveredDevice connectedDevice) {
        synchronizedDeviceHolder.setObject(connectedDevice);
        countKnownSynchronizedDeviceConnectedCalled.incrementAndGet();
      }

      @Override
      public void knownSynchronizedDeviceDisconnected(DiscoveredDevice disconnectedDevice) {
        countKnownSynchronizedDeviceDisconnectedCalled.incrementAndGet();
      }
    });


    devicesDiscovererListener.deviceFound(underTest.getDeviceInfoKey(new DiscoveredDevice(remoteDevice, REMOTE_DEVICE_ADDRESS)), REMOTE_DEVICE_ADDRESS);


    assertThat(countKnownSynchronizedDeviceConnectedCalled.get(), is(0));
    assertThat(countKnownSynchronizedDeviceDisconnectedCalled.get(), is(0));

    assertThat(synchronizedDeviceHolder.isObjectSet(), is(false));
  }


  protected void assertDiscoveredDeviceHasCorrectlyBeenSet(DiscoveredDevice discoveredDevice) {
    assertThat(discoveredDevice.getAddress(), is(REMOTE_DEVICE_ADDRESS));

    Device device = discoveredDevice.getDevice();

    assertThat(device.getUniqueDeviceId(), is(REMOTE_DEVICE_UNIQUE_DEVICE_ID));
    assertThat(device.getName(), is(REMOTE_DEVICE_NAME));
    assertThat(device.getOsName(), is(REMOTE_DEVICE_OS_NAME));
    assertThat(device.getOsVersion(), is(REMOTE_DEVICE_OS_VERSION));
    assertThat(device.getOsType(), is(REMOTE_DEVICE_OS_TYPE));
    assertThat(device.getDescription(), is(REMOTE_DEVICE_DESCRIPTION));
  }


  protected DiscoveredDevice mockUnknownDiscoveredDevice() {
    DiscoveredDevice discoveredDevice = mockDiscoveredDevice();

    underTest.unknownDevices.put(underTest.getDeviceInfoKey(discoveredDevice), discoveredDevice);

    return discoveredDevice;
  }

  protected DiscoveredDevice mockIgnoredDiscoveredDevice() {
    DiscoveredDevice discoveredDevice = mockDiscoveredDevice();

    underTest.knownIgnoredDevices.put(underTest.getDeviceInfoKey(discoveredDevice), discoveredDevice);

    localConfig.addIgnoredDevice(discoveredDevice.getDevice());
    entityManager.updateEntity(localConfig);

    return discoveredDevice;
  }

  protected DiscoveredDevice mockDiscoveredDevice() {
    Device remoteDevice = new Device("Remote");
    DiscoveredDevice discoveredDevice = new DiscoveredDevice(remoteDevice, "1-1-1-Love");
    discoveredDevice.setMessagesPort(MESSAGES_RECEIVER_PORT);
    entityManager.persistEntity(remoteDevice);

    underTest.discoveredDevices.put(underTest.getDeviceInfoKey(discoveredDevice), discoveredDevice);

    return discoveredDevice;
  }

  protected void mockRequestStartSynchronization(final RequestStartSynchronizationResult result) {
    mockRequestStartSynchronization(result, -1);
  }

  protected void mockRequestStartSynchronization(final RequestStartSynchronizationResult result, final int synchronizationPort) {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        SendRequestCallback callback = (SendRequestCallback)invocation.getArguments()[1];
        callback.done(new Response(new RequestStartSynchronizationResponseBody(result, synchronizationPort)));
        return null;
      }
    }).when(clientCommunicator).requestStartSynchronization(any(DiscoveredDevice.class), any(SendRequestCallback.class));
  }

}
