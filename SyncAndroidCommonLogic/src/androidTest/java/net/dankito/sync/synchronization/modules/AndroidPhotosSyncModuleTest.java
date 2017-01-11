package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.persistence.IEntityManager;

import org.junit.Assert;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidPhotosSyncModuleTest extends AndroidSyncModuleTestBase {


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext, IEntityManager entityManager) {
    return new AndroidPhotosSyncModule(appContext, entityManager);
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

  @NonNull
  protected String getIdColumnForEntity() {
    return MediaStore.Images.Media._ID; // TODO: is this correct?
  }

}
