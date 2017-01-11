package net.dankito.sync.synchronization;

import com.couchbase.lite.CouchbaseLiteException;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkConfigurationManager;
import net.dankito.sync.devices.NetworkConfigurationManager;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJava;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.synchronization.helper.TestDevicesManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.NetworkHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 05/09/16.
 */
public class CouchbaseLiteSyncManagerTest {

  public static final String DEVICE_1_ID = "1";

  public static final String DEVICE_2_ID = "2";

  public static final int DEVICE_1_SYNCHRONIZATION_PORT = 23456;

  public static final int DEVICE_2_SYNCHRONIZATION_PORT = 23458;


  protected INetworkConfigurationManager configurationManager1 = new NetworkConfigurationManager();
  protected INetworkConfigurationManager configurationManager2 = new NetworkConfigurationManager();

  protected TestDevicesManager devicesManager1 = new TestDevicesManager();
  protected TestDevicesManager devicesManager2 = new TestDevicesManager();

  protected CouchbaseLiteEntityManagerBase entityManager1;
  protected CouchbaseLiteEntityManagerBase entityManager2;

  protected CouchbaseLiteSyncManager syncManager1;
  protected CouchbaseLiteSyncManager syncManager2;

  protected DiscoveredDevice device1;
  protected DiscoveredDevice device2;


  @Before
  public void setUp() throws Exception {
    IThreadPool threadPool = new ThreadPool();
    NetworkHelper networkHelper = new NetworkHelper();

    entityManager1 = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData/db01_" + System.currentTimeMillis(), 1));
    entityManager2 = new CouchbaseLiteEntityManagerJava(new EntityManagerConfiguration("testData/db01_" + System.currentTimeMillis(), 1));

    syncManager1 = new CouchbaseLiteSyncManager(entityManager1, configurationManager1, devicesManager1, threadPool, DEVICE_1_SYNCHRONIZATION_PORT, true);
    syncManager2 = new CouchbaseLiteSyncManager(entityManager2, configurationManager2, devicesManager2, threadPool, DEVICE_2_SYNCHRONIZATION_PORT, true);

    device1 = new DiscoveredDevice(new Device(DEVICE_1_ID), networkHelper.getIPAddressString(true));
    device1.setSynchronizationPort(DEVICE_1_SYNCHRONIZATION_PORT);

    device2 = new DiscoveredDevice(new Device(DEVICE_2_ID), networkHelper.getIPAddressString(true));
    device2.setSynchronizationPort(DEVICE_2_SYNCHRONIZATION_PORT);

    devicesManager1.simulateKnownSynchronizedDeviceConnected(device2);
    devicesManager2.simulateKnownSynchronizedDeviceConnected(device1);
  }


  @After
  public void tearDown() throws CouchbaseLiteException {
    syncManager1.stopSynchronizationWithDevice(device2);
    syncManager2.stopSynchronizationWithDevice(device1);

    syncManager1.stop();
    syncManager2.stop();

    entityManager1.close();
    entityManager2.close();

    entityManager1.getDatabase().delete();
    entityManager2.getDatabase().delete();
  }


  @Test
  public void persistEntityWithoutRelation_EntityGetSynchronizedCorrectly() {
    final CountDownLatch synchronizationLatch = new CountDownLatch(1);
    final List<BaseEntity> synchronizedEntities = new ArrayList<>();

    syncManager2.addSynchronizationListener(new SynchronizationListener() {
      @Override
      public void entitySynchronized(BaseEntity entity) {
        synchronizedEntities.add(entity);
        synchronizationLatch.countDown();
      }
    });

    ContactSyncEntity testEntity = new ContactSyncEntity();
    testEntity.setDisplayName("Gandhi");
    testEntity.setGivenName("");
    testEntity.setFamilyName("Gandhi");
    entityManager1.persistEntity(testEntity);

    try { synchronizationLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, synchronizedEntities.size());

    ContactSyncEntity synchronizedEntity = (ContactSyncEntity)synchronizedEntities.get(0);
    Assert.assertEquals(testEntity.getId(), synchronizedEntity.getId());
    Assert.assertEquals(testEntity.getDisplayName(), synchronizedEntity.getDisplayName());
    Assert.assertEquals(testEntity.getGivenName(), synchronizedEntity.getGivenName());
    Assert.assertEquals(testEntity.getFamilyName(), synchronizedEntity.getFamilyName());
  }

}
