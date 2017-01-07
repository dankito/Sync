package net.dankito.sync.persistence.model;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJavaTestBase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;


public class DeviceModelTest extends CouchbaseLiteEntityManagerJavaTestBase {

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
    return Device.class;
  }


  protected BaseEntity createTestEntity() {
    Device testEntity = new Device(TEST_UNIQUE_DEVICE_ID);

    testEntity.setOsType(TEST_OS_TYPE);
    testEntity.setOsName(TEST_OS_NAME);
    testEntity.setOsVersion(TEST_OS_VERSION);
    testEntity.setDescription(TEST_DESCRIPTION);

    return testEntity;
  }

  protected void updateEntity(BaseEntity testEntity) {
    Device entity = (Device)testEntity;

    entity.setOsType(TEST_UPDATED_OS_TYPE);
    entity.setOsName(TEST_UPDATED_OS_NAME);
    entity.setOsVersion(TEST_UPDATED_OS_VERSION);
    entity.setDescription(TEST_UPDATED_DESCRIPTION);

  }

  @Override
  protected List<BaseEntity> updateEntityReferences(BaseEntity testEntity) {
    return new ArrayList<>();
  }


  protected void assertEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    Device entity = (Device)retrievedEntity;

    Assert.assertEquals(TEST_UNIQUE_DEVICE_ID, entity.getUniqueDeviceId());
    Assert.assertEquals(TEST_OS_TYPE, entity.getOsType());
    Assert.assertEquals(TEST_OS_NAME, entity.getOsName());
    Assert.assertEquals(TEST_OS_VERSION, entity.getOsVersion());
    Assert.assertEquals(TEST_DESCRIPTION, entity.getDescription());
  }

  protected void assertUpdatedEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    Device entity = (Device)retrievedEntity;

    Assert.assertEquals(TEST_UNIQUE_DEVICE_ID, entity.getUniqueDeviceId());
    Assert.assertEquals(TEST_UPDATED_OS_TYPE, entity.getOsType());
    Assert.assertEquals(TEST_UPDATED_OS_NAME, entity.getOsName());
    Assert.assertEquals(TEST_UPDATED_OS_VERSION, entity.getOsVersion());
    Assert.assertEquals(TEST_UPDATED_DESCRIPTION, entity.getDescription());
  }

  @Override
  protected void assertUpdatedEntityReferencesHaveBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {

  }

}
