package net.dankito.sync.synchronization.modules;

import android.content.Context;

import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidExternalPhotosSyncModuleTest extends AndroidPhotosSyncModuleBaseTest {

  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context context, Localization localization, IThreadPool threadPool) {
    return new AndroidExternalPhotosSyncModule(context, localization, threadPool, new FileSyncService(entityManager), new JavaFileStorageService());
  }

}
