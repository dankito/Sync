package net.dankito.sync.devices;


import net.dankito.devicediscovery.UdpDevicesDiscoverer;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.communication.IClientCommunicator;
import net.dankito.sync.communication.TcpSocketClientCommunicator;
import net.dankito.sync.communication.callback.ClientCommunicatorListener;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DevicesManagerTest {

  protected static final int MESSAGES_RECEIVER_PORT = 54321;


  protected DevicesManager underTest;

  protected IEntityManager entityManager;

  protected IDataManager dataManager;

  protected IClientCommunicator clientCommunicator;

  protected LocalConfig localConfig;


  @Before
  public void setUp() throws Exception {
    entityManager = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData", 1));

    Device localDevice = new Device("Local");
    this.localConfig = new LocalConfig(localDevice);
    entityManager.persistEntity(localConfig);

    final INetworkSettings networkSettings = new NetworkSettings(localDevice);

    dataManager = Mockito.mock(IDataManager.class);
    Mockito.when(dataManager.getLocalConfig()).thenReturn(localConfig);

    IThreadPool threadPool = new ThreadPool();

    clientCommunicator = new TcpSocketClientCommunicator(networkSettings, threadPool);

    underTest = new DevicesManager(new UdpDevicesDiscoverer(threadPool), clientCommunicator, dataManager, networkSettings, entityManager);


    final CountDownLatch countDownLatch = new CountDownLatch(1);

    clientCommunicator.start(MESSAGES_RECEIVER_PORT, new ClientCommunicatorListener() {
      @Override
      public void started(boolean couldStartMessagesReceiver, int messagesReceiverPort, Exception startException) {
        networkSettings.setMessagePort(messagesReceiverPort);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.SECONDS); } catch(Exception ignored) { }
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

}
