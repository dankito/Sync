package net.dankito.sync.persistence.model;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.OsType;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJavaTestBase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LocalConfigModelTest extends CouchbaseLiteEntityManagerJavaTestBase {

  protected static final String TEST_UNIQUE_DEVICE_ID = "270388";

  protected static final String TEST_NAME = "Lenovo T530";

  protected static final OsType TEST_OS_TYPE = OsType.DESKTOP;

  protected static final String TEST_OS_NAME = "Linux";

  protected static final String TEST_OS_VERSION = "3.12";

  protected static final String TEST_DESCRIPTION = "Apple free";


  protected static final String TEST_UPDATED_NAME = "Samsung Galaxy S3";

  protected static final OsType TEST_UPDATED_OS_TYPE = OsType.ANDROID;

  protected static final String TEST_UPDATED_OS_NAME = "Android";

  protected static final String TEST_UPDATED_OS_VERSION = "6.0";

  protected static final String TEST_UPDATED_DESCRIPTION = "Apple and Microsoft free";


  protected static final String TEST_SYNCHRONIZED_DEVICE_01_NAME = "Synced1";
  protected static final String TEST_SYNCHRONIZED_DEVICE_02_NAME = "Synced2";
  protected static final String TEST_SYNCHRONIZED_DEVICE_03_NAME = "Synced3";

  protected static final String TEST_UPDATED_SYNCHRONIZED_DEVICE_01_NAME = "Synced1_Updated";
  protected static final String TEST_UPDATED_SYNCHRONIZED_DEVICE_02_NAME = "Synced2_Updated";
  protected static final String TEST_UPDATED_SYNCHRONIZED_DEVICE_03_NAME = "Synced3_Updated";


  protected static final String TEST_IGNORED_DEVICE_01_NAME = "Ignored1";
  protected static final String TEST_IGNORED_DEVICE_02_NAME = "Ignored2";

  protected static final String TEST_UPDATED_IGNORED_DEVICE_01_NAME = "Ignored1_Updated";
  protected static final String TEST_UPDATED_IGNORED_DEVICE_02_NAME = "Ignored2_Updated";



  protected Class<? extends BaseEntity> getEntityClass() {
    return LocalConfig.class;
  }


  protected BaseEntity createTestEntity() {
    Device testDevice = new Device(TEST_UNIQUE_DEVICE_ID);

    testDevice.setName(TEST_NAME);
    testDevice.setOsType(TEST_OS_TYPE);
    testDevice.setOsName(TEST_OS_NAME);
    testDevice.setOsVersion(TEST_OS_VERSION);
    testDevice.setDescription(TEST_DESCRIPTION);

    LocalConfig testEntity = new LocalConfig(testDevice);

    testEntity.addSynchronizedDevice(new Device(TEST_SYNCHRONIZED_DEVICE_01_NAME));
    testEntity.addSynchronizedDevice(new Device(TEST_SYNCHRONIZED_DEVICE_02_NAME));
    testEntity.addSynchronizedDevice(new Device(TEST_SYNCHRONIZED_DEVICE_03_NAME));

    testEntity.addIgnoredDevice(new Device(TEST_IGNORED_DEVICE_01_NAME));
    testEntity.addIgnoredDevice(new Device(TEST_IGNORED_DEVICE_02_NAME));

    for(Device device : testEntity.getSynchronizedDevices()) {
      underTest.persistEntity(device);
    }
    for(Device device : testEntity.getIgnoredDevices()) {
      underTest.persistEntity(device);
    }

    return testEntity;
  }

  protected void updateEntity(BaseEntity testEntity) {
    LocalConfig entity = (LocalConfig)testEntity;

  }

  @Override
  protected List<BaseEntity> updateEntityReferences(BaseEntity testEntity) {
    List<BaseEntity> updatedReferences = new ArrayList<>();

    LocalConfig entity = (LocalConfig)testEntity;

    Device testDevice = entity.getLocalDevice();
    updatedReferences.add(testDevice);

    testDevice.setName(TEST_UPDATED_NAME);
    testDevice.setOsType(TEST_UPDATED_OS_TYPE);
    testDevice.setOsName(TEST_UPDATED_OS_NAME);
    testDevice.setOsVersion(TEST_UPDATED_OS_VERSION);
    testDevice.setDescription(TEST_UPDATED_DESCRIPTION);

    List<Device> synchronizedDevices = sortDevicesAfterTheirUniqueIds(entity.getSynchronizedDevices());
    synchronizedDevices.get(0).setName(TEST_UPDATED_SYNCHRONIZED_DEVICE_01_NAME);
    synchronizedDevices.get(1).setName(TEST_UPDATED_SYNCHRONIZED_DEVICE_02_NAME);
    synchronizedDevices.get(2).setName(TEST_UPDATED_SYNCHRONIZED_DEVICE_03_NAME);
    updatedReferences.add(synchronizedDevices.get(0));
    updatedReferences.add(synchronizedDevices.get(1));
    updatedReferences.add(synchronizedDevices.get(2));

    List<Device> ignoredDevices = sortDevicesAfterTheirUniqueIds(entity.getIgnoredDevices());
    ignoredDevices.get(0).setName(TEST_UPDATED_IGNORED_DEVICE_01_NAME);
    ignoredDevices.get(1).setName(TEST_UPDATED_IGNORED_DEVICE_02_NAME);
    updatedReferences.add(ignoredDevices.get(0));
    updatedReferences.add(ignoredDevices.get(1));

    return updatedReferences;
  }


  protected void assertEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    LocalConfig entity = (LocalConfig)retrievedEntity;

    Device testDevice = entity.getLocalDevice();

    Assert.assertEquals(TEST_UNIQUE_DEVICE_ID, testDevice.getUniqueDeviceId());
    Assert.assertEquals(TEST_NAME, testDevice.getName());
    Assert.assertEquals(TEST_OS_TYPE, testDevice.getOsType());
    Assert.assertEquals(TEST_OS_NAME, testDevice.getOsName());
    Assert.assertEquals(TEST_OS_VERSION, testDevice.getOsVersion());
    Assert.assertEquals(TEST_DESCRIPTION, testDevice.getDescription());

    List<Device> synchronizedDevices = sortDevicesAfterTheirUniqueIds(entity.getSynchronizedDevices());
    Assert.assertEquals(3, synchronizedDevices.size());
    Assert.assertEquals(TEST_SYNCHRONIZED_DEVICE_01_NAME, synchronizedDevices.get(0).getUniqueDeviceId());
    Assert.assertEquals(TEST_SYNCHRONIZED_DEVICE_02_NAME, synchronizedDevices.get(1).getUniqueDeviceId());
    Assert.assertEquals(TEST_SYNCHRONIZED_DEVICE_03_NAME, synchronizedDevices.get(2).getUniqueDeviceId());

    List<Device> ignoredDevices = sortDevicesAfterTheirUniqueIds(entity.getIgnoredDevices());
    Assert.assertEquals(2, ignoredDevices.size());
    Assert.assertEquals(TEST_IGNORED_DEVICE_01_NAME, ignoredDevices.get(0).getUniqueDeviceId());
    Assert.assertEquals(TEST_IGNORED_DEVICE_02_NAME, ignoredDevices.get(1).getUniqueDeviceId());
  }

  protected List<Device> sortDevicesAfterTheirUniqueIds(List<Device> devices) {
    List<Device> sortedDevices = new ArrayList<>(devices);

    Collections.sort(sortedDevices, new Comparator<Device>() {
      @Override
      public int compare(Device device1, Device device2) {
        return device1.getUniqueDeviceId().compareTo(device2.getUniqueDeviceId());
      }
    });

    return sortedDevices;
  }


  protected void assertUpdatedEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    LocalConfig entity = (LocalConfig)retrievedEntity;
  }

  @Override
  protected void assertUpdatedEntityReferencesHaveBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    LocalConfig entity = (LocalConfig)retrievedEntity;

    Device testDevice = entity.getLocalDevice();

    Assert.assertEquals(TEST_UNIQUE_DEVICE_ID, testDevice.getUniqueDeviceId());
    Assert.assertEquals(TEST_UPDATED_NAME, testDevice.getName());
    Assert.assertEquals(TEST_UPDATED_OS_TYPE, testDevice.getOsType());
    Assert.assertEquals(TEST_UPDATED_OS_NAME, testDevice.getOsName());
    Assert.assertEquals(TEST_UPDATED_OS_VERSION, testDevice.getOsVersion());
    Assert.assertEquals(TEST_UPDATED_DESCRIPTION, testDevice.getDescription());

    List<Device> synchronizedDevices = sortDevicesAfterTheirUniqueIds(entity.getSynchronizedDevices());
    Assert.assertEquals(3, synchronizedDevices.size());
    Assert.assertEquals(TEST_UPDATED_SYNCHRONIZED_DEVICE_01_NAME, synchronizedDevices.get(0).getName());
    Assert.assertEquals(TEST_UPDATED_SYNCHRONIZED_DEVICE_02_NAME, synchronizedDevices.get(1).getName());
    Assert.assertEquals(TEST_UPDATED_SYNCHRONIZED_DEVICE_03_NAME, synchronizedDevices.get(2).getName());

    List<Device> ignoredDevices = sortDevicesAfterTheirUniqueIds(entity.getIgnoredDevices());
    Assert.assertEquals(2, ignoredDevices.size());
    Assert.assertEquals(TEST_UPDATED_IGNORED_DEVICE_01_NAME, ignoredDevices.get(0).getName());
    Assert.assertEquals(TEST_UPDATED_IGNORED_DEVICE_02_NAME, ignoredDevices.get(1).getName());
  }

}
