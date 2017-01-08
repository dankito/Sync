package net.dankito.sync.devices;


import net.dankito.devicediscovery.UdpDevicesDiscoverer;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DevicesManagerTest {

  protected DevicesManager underTest;

  protected IEntityManager entityManager;

  protected LocalConfig localConfig;


  @Before
  public void setUp() throws Exception {
    entityManager = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData", 1));

    Device localDevice = new Device("Local");
    this.localConfig = new LocalConfig(localDevice);
    entityManager.persistEntity(localConfig);

    underTest = new DevicesManager(new UdpDevicesDiscoverer(new ThreadPool()), entityManager, localConfig);
  }

  @After
  public void tearDown() {
    underTest.stop();

    deleteFolderRecursively(entityManager.getDatabasePath());
  }


  @Test
  public void startSynchronizingWithDevice() {
    DiscoveredDevice discoveredDevice = mockUnknownDiscoveredDevice();

    Assert.assertFalse(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(1, underTest.unknownDevices.size());
    Assert.assertEquals(0, underTest.knownSynchronizedDevices.size());


    underTest.startSynchronizingWithDevice(discoveredDevice, new ArrayList<SyncModuleConfiguration>());


    Assert.assertTrue(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    List<SyncConfiguration> syncConfigurations = entityManager.getAllEntitiesOfType(SyncConfiguration.class);
    Assert.assertEquals(1, syncConfigurations.size());

    Assert.assertEquals(0, underTest.unknownDevices.size());
    Assert.assertEquals(1, underTest.knownSynchronizedDevices.size());
  }


  @Test
  public void stopSynchronizingWithDevice() {
    DiscoveredDevice discoveredDevice = mockUnknownDiscoveredDevice();

    underTest.startSynchronizingWithDevice(discoveredDevice, new ArrayList<SyncModuleConfiguration>());

    Assert.assertTrue(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    Assert.assertEquals(1, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(0, underTest.unknownDevices.size());
    Assert.assertEquals(1, underTest.knownSynchronizedDevices.size());


    underTest.stopSynchronizingWithDevice(discoveredDevice);


    Assert.assertFalse(localConfig.getSynchronizedDevices().contains(discoveredDevice.getDevice()));
    Assert.assertEquals(0, entityManager.getAllEntitiesOfType(SyncConfiguration.class).size());

    Assert.assertEquals(1, underTest.unknownDevices.size());
    Assert.assertEquals(0, underTest.knownSynchronizedDevices.size());
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


  protected DiscoveredDevice mockUnknownDiscoveredDevice() {
    Device remoteDevice = new Device("Remote");
    DiscoveredDevice discoveredDevice = new DiscoveredDevice(remoteDevice, "1-1-1-Love");
    entityManager.persistEntity(remoteDevice);

    underTest.unknownDevices.put(underTest.getDeviceInfoFromDevice(remoteDevice), discoveredDevice);
    underTest.discoveredDevices.put(underTest.getDeviceInfoFromDevice(remoteDevice), discoveredDevice);
    return discoveredDevice;
  }


  protected void deleteFolderRecursively(String path) {
    deleteRecursively(new File(path));
  }

  protected void deleteRecursively(File file) {
    if(file.isDirectory()) {
      for(File containingFile : file.listFiles()) {
        deleteRecursively(containingFile);
      }
    }

    file.delete();
  }

}
