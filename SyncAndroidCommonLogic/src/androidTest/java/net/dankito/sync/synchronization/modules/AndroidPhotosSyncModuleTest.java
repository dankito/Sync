package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

import org.junit.Assert;

import java.util.Date;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidPhotosSyncModuleTest extends AndroidSyncModuleTestBase {

  protected static final byte[] TEST_IMAGE_DATA = new byte[] { 54, 64, -118, 73 };

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


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context context, IEntityManager entityManager, IThreadPool threadPool) {
    return new AndroidPhotosSyncModule(context, entityManager, threadPool, new JavaFileStorageService());
  }

  @Override
  protected byte[] getSyncEntityData(SyncEntity entity) {
    return TEST_IMAGE_DATA;
  }

  @NonNull
  @Override
  protected SyncEntity createTestEntity() {
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

  @Override
  protected void updateTestEntity(SyncEntity entityToUpdate) {

  }


  @Override
  protected void testReadEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ImageFileSyncEntity);

    ImageFileSyncEntity entity = (ImageFileSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getName());
    Assert.assertNotNull(entity.getFilePath());
  }

  @Override
  protected void testIfEntryHasSuccessfullyBeenAdded(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ImageFileSyncEntity);

    ImageFileSyncEntity entity = (ImageFileSyncEntity)entityToTest;

    Assert.assertEquals(TEST_IMAGE_NAME, entity.getName());
    Assert.assertEquals(TEST_FILE_PATH, entity.getFilePath());
    Assert.assertEquals(TEST_MIME_TYPE, entity.getMimeType());
    Assert.assertEquals(TEST_DESCRIPTION, entity.getDescription());
    Assert.assertEquals(TEST_HEIGHT, entity.getHeight());
    Assert.assertEquals(TEST_WIDTH, entity.getWidth());
    Assert.assertEquals(TEST_FILE_SIZE, entity.getFileSize());
    Assert.assertEquals(TEST_LATITUDE, entity.getLatitude(), 0.01);
    Assert.assertEquals(TEST_LATITUDE, entity.getLongitude(), 0.01);
    Assert.assertEquals(TEST_IMAGE_TAKEN_ON, entity.getImageTakenOn());
  }

  @Override
  protected void testIfEntryHasSuccessfullyBeenUpdated(SyncEntity entityToTest) {

  }

  @NonNull
  protected String getIdColumnForEntity() {
    return MediaStore.Images.Media._ID; // TODO: is this correct?
  }

}
