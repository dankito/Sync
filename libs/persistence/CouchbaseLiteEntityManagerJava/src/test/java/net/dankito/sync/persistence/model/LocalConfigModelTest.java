package net.dankito.sync.persistence.model;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.OsType;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJavaTestBase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;


public class LocalConfigModelTest extends CouchbaseLiteEntityManagerJavaTestBase {

  protected static final String TEST_UNIQUE_DEVICE_ID = "270388";

  protected static final OsType TEST_OS_TYPE = OsType.DESKTOP;

  protected static final String TEST_OS_NAME = "Linux";

  protected static final String TEST_OS_VERSION = "3.12";

  protected static final String TEST_DESCRIPTION = "Apple free";


  protected static final OsType TEST_UPDATED_OS_TYPE = OsType.ANDROID;

  protected static final String TEST_UPDATED_OS_NAME = "Android";

  protected static final String TEST_UPDATED_OS_VERSION = "6.0";

  protected static final String TEST_UPDATED_DESCRIPTION = "Apple and Microsoft free";



  protected Class<? extends BaseEntity> getEntityClass() {
    return LocalConfig.class;
  }


  protected BaseEntity createTestEntity() {
    Device testDevice = new Device(TEST_UNIQUE_DEVICE_ID);

    testDevice.setOsType(TEST_OS_TYPE);
    testDevice.setOsName(TEST_OS_NAME);
    testDevice.setOsVersion(TEST_OS_VERSION);
    testDevice.setDescription(TEST_DESCRIPTION);

    LocalConfig testEntity = new LocalConfig(testDevice);
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

    testDevice.setOsType(TEST_UPDATED_OS_TYPE);
    testDevice.setOsName(TEST_UPDATED_OS_NAME);
    testDevice.setOsVersion(TEST_UPDATED_OS_VERSION);
    testDevice.setDescription(TEST_UPDATED_DESCRIPTION);

    return updatedReferences;
  }


  protected void assertEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    LocalConfig entity = (LocalConfig)retrievedEntity;

    Device testDevice = entity.getLocalDevice();

    Assert.assertEquals(TEST_UNIQUE_DEVICE_ID, testDevice.getUniqueDeviceId());
    Assert.assertEquals(TEST_OS_TYPE, testDevice.getOsType());
    Assert.assertEquals(TEST_OS_NAME, testDevice.getOsName());
    Assert.assertEquals(TEST_OS_VERSION, testDevice.getOsVersion());
    Assert.assertEquals(TEST_DESCRIPTION, testDevice.getDescription());
  }

  protected void assertUpdatedEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    LocalConfig entity = (LocalConfig)retrievedEntity;
  }

  @Override
  protected void assertUpdatedEntityReferencesHaveBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    LocalConfig entity = (LocalConfig)retrievedEntity;

    Device testDevice = entity.getLocalDevice();

    Assert.assertEquals(TEST_UNIQUE_DEVICE_ID, testDevice.getUniqueDeviceId());
    Assert.assertEquals(TEST_UPDATED_OS_TYPE, testDevice.getOsType());
    Assert.assertEquals(TEST_UPDATED_OS_NAME, testDevice.getOsName());
    Assert.assertEquals(TEST_UPDATED_OS_VERSION, testDevice.getOsVersion());
    Assert.assertEquals(TEST_UPDATED_DESCRIPTION, testDevice.getDescription());
  }

}
