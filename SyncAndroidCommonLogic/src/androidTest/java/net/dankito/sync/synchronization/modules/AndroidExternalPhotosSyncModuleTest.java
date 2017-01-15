package net.dankito.sync.synchronization.modules;

import android.content.Context;

import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidExternalPhotosSyncModuleTest extends AndroidPhotosSyncModuleBaseTest {

  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context context, IEntityManager entityManager, IThreadPool threadPool) {
    return new AndroidExternalPhotosSyncModule(context, threadPool, new JavaFileStorageService());
  }

}
