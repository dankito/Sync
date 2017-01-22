package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;


public class AndroidExternalPhotosSyncModule extends AndroidPhotosSyncModuleBase {

  public AndroidExternalPhotosSyncModule(Context context, Localization localization, IPermissionsManager permissionsManager, IThreadPool threadPool, FileSyncService fileSyncService, IFileStorageService fileStorageService) {
    super(context, localization, permissionsManager, threadPool, fileSyncService, fileStorageService);
  }


  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.ANDROID_EXTERNAL_PHOTOS.getTypeName();
  }


  @Override
  protected Uri getContentUri() {
    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  }

  @Override
  public String getRootFolder() {
    return Environment.getExternalStorageDirectory().getAbsolutePath();
  }

}
