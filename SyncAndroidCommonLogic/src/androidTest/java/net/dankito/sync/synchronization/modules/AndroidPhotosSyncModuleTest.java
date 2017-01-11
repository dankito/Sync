package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.IThreadPool;

import org.junit.Assert;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidPhotosSyncModuleTest extends AndroidSyncModuleTestBase {


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext, IEntityManager entityManager, IThreadPool threadPool) {
    return new AndroidPhotosSyncModule(appContext, entityManager, threadPool);
  }

  @NonNull
  @Override
  protected SyncEntity createTestEntity() {
    return null;
  }

  @Override
  protected void updateTestEntity(SyncEntity entityToUpdate) {

  }


  @Override
  protected void testReadEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof ImageFileSyncEntity);

    ImageFileSyncEntity entity = (ImageFileSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getFilePath());
    Assert.assertNotNull(entity.getName());
    Assert.assertNotNull(entity.getMimeType());
    Assert.assertNotNull(entity.getImageTakenOn());
  }

  @Override
  protected void testIfEntryHasSuccessfullyBeenAdded(SyncEntity entity) {

  }

  @Override
  protected void testIfEntryHasSuccessfullyBeenUpdated(SyncEntity entity) {

  }

  @NonNull
  protected String getIdColumnForEntity() {
    return MediaStore.Images.Media._ID; // TODO: is this correct?
  }

}
