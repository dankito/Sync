package net.dankito.sync.persistence.model;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerJavaTestBase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ImageFileSyncEntityModelTest extends CouchbaseLiteEntityManagerJavaTestBase {

  protected static final String TEST_IMAGE_NAME = "test.png";
  protected static final String TEST_FILE_PATH = "tmp/test.png";
  protected static final String TEST_MIME_TYPE = "image/png";
  protected static final String TEST_DESCRIPTION = "Test image";
  protected static final int TEST_HEIGHT = 1080;
  protected static final int TEST_WIDTH = 1920;
  protected static final int TEST_FILE_SIZE = TEST_HEIGHT * TEST_WIDTH;
  protected static final Date TEST_IMAGE_TAKEN_ON = new Date();
  protected static final double TEST_LATITUDE = 65.4;
  protected static final double TEST_LONGITUDE = 43.2;
  protected static final int TEST_ORIENTATION = 90;

  protected static final String TEST_UPDATED_IMAGE_NAME = "test.jpg";
  protected static final String TEST_UPDATED_FILE_PATH = "tmp/test.jpg";
  protected static final String TEST_UPDATED_MIME_TYPE = "image/jpg";
  protected static final String TEST_UPDATED_DESCRIPTION = "Test image updated";
  protected static final int TEST_UPDATED_HEIGHT = 720;
  protected static final int TEST_UPDATED_WIDTH = 1280;
  protected static final int TEST_UPDATED_FILE_SIZE = TEST_HEIGHT * TEST_WIDTH;
  protected static final Date TEST_UPDATED_IMAGE_TAKEN_ON = new Date(new Date().getTime() + 1000 * 60);
  protected static final double TEST_UPDATED_LATITUDE = 45.6;
  protected static final double TEST_UPDATED_LONGITUDE = 23.4;
  protected static final int TEST_UPDATED_ORIENTATION = 180;


  protected Class<? extends BaseEntity> getEntityClass() {
    return ImageFileSyncEntity.class;
  }


  protected BaseEntity createTestEntity() {
    ImageFileSyncEntity testEntity = new ImageFileSyncEntity();

    testEntity.setName(TEST_IMAGE_NAME);
    testEntity.setFilePath(TEST_FILE_PATH);
    testEntity.setMimeType(TEST_MIME_TYPE);
    testEntity.setDescription(TEST_DESCRIPTION);
    testEntity.setHeight(TEST_HEIGHT);
    testEntity.setWidth(TEST_WIDTH);
    testEntity.setFileSize(TEST_FILE_SIZE);
    testEntity.setImageTakenOn(TEST_IMAGE_TAKEN_ON);
    testEntity.setLatitude(TEST_LATITUDE);
    testEntity.setLongitude(TEST_LONGITUDE);
    testEntity.setOrientation(TEST_ORIENTATION);

    return testEntity;
  }

  protected void updateEntity(BaseEntity testEntity) {
    ImageFileSyncEntity entity = (ImageFileSyncEntity)testEntity;

    entity.setName(TEST_UPDATED_IMAGE_NAME);
    entity.setFilePath(TEST_UPDATED_FILE_PATH);
    entity.setMimeType(TEST_UPDATED_MIME_TYPE);
    entity.setDescription(TEST_UPDATED_DESCRIPTION);
    entity.setHeight(TEST_UPDATED_HEIGHT);
    entity.setWidth(TEST_UPDATED_WIDTH);
    entity.setFileSize(TEST_UPDATED_FILE_SIZE);
    entity.setImageTakenOn(TEST_UPDATED_IMAGE_TAKEN_ON);
    entity.setLatitude(TEST_UPDATED_LATITUDE);
    entity.setLongitude(TEST_UPDATED_LONGITUDE);
    entity.setOrientation(TEST_UPDATED_ORIENTATION);
  }

  @Override
  protected List<BaseEntity> updateEntityReferences(BaseEntity testEntity) {
    return new ArrayList<>();
  }


  protected void assertEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    ImageFileSyncEntity entity = (ImageFileSyncEntity)retrievedEntity;

    Assert.assertEquals(TEST_IMAGE_NAME, entity.getName());
    Assert.assertEquals(TEST_FILE_PATH, entity.getFilePath());
    Assert.assertEquals(TEST_MIME_TYPE, entity.getMimeType());
    Assert.assertEquals(TEST_DESCRIPTION, entity.getDescription());
    Assert.assertEquals(TEST_HEIGHT, entity.getHeight());
    Assert.assertEquals(TEST_WIDTH, entity.getWidth());
    Assert.assertEquals(TEST_FILE_SIZE, entity.getFileSize());
    Assert.assertEquals(TEST_IMAGE_TAKEN_ON, entity.getImageTakenOn());
    Assert.assertEquals(TEST_LATITUDE, entity.getLatitude(), 0.01);
    Assert.assertEquals(TEST_LONGITUDE, entity.getLongitude(), 0.01);
    Assert.assertEquals(TEST_ORIENTATION, entity.getOrientation());
  }

  protected void assertUpdatedEntityHasBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {
    ImageFileSyncEntity entity = (ImageFileSyncEntity)retrievedEntity;

    Assert.assertEquals(TEST_UPDATED_IMAGE_NAME, entity.getName());
    Assert.assertEquals(TEST_UPDATED_FILE_PATH, entity.getFilePath());
    Assert.assertEquals(TEST_UPDATED_MIME_TYPE, entity.getMimeType());
    Assert.assertEquals(TEST_UPDATED_DESCRIPTION, entity.getDescription());
    Assert.assertEquals(TEST_UPDATED_HEIGHT, entity.getHeight());
    Assert.assertEquals(TEST_UPDATED_WIDTH, entity.getWidth());
    Assert.assertEquals(TEST_UPDATED_FILE_SIZE, entity.getFileSize());
    Assert.assertEquals(TEST_UPDATED_IMAGE_TAKEN_ON, entity.getImageTakenOn());
    Assert.assertEquals(TEST_UPDATED_LATITUDE, entity.getLatitude(), 0.01);
    Assert.assertEquals(TEST_UPDATED_LONGITUDE, entity.getLongitude(), 0.01);
    Assert.assertEquals(TEST_UPDATED_ORIENTATION, entity.getOrientation());
  }

  @Override
  protected void assertUpdatedEntityReferencesHaveBeenCorrectlyRetrieved(BaseEntity retrievedEntity) {

  }

}
