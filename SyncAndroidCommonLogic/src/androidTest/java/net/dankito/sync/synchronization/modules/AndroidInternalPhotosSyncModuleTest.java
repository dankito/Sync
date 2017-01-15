package net.dankito.sync.synchronization.modules;

import android.content.Context;

import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.JavaFileStorageService;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidInternalPhotosSyncModuleTest extends AndroidPhotosSyncModuleBaseTest {

  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context context, IThreadPool threadPool) {
    return new AndroidInternalPhotosSyncModule(context, threadPool, new JavaFileStorageService());
  }

}
