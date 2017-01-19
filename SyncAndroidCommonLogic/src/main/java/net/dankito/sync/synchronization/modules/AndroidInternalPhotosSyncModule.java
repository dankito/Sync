package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.localization.Localization;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;


public class AndroidInternalPhotosSyncModule extends AndroidPhotosSyncModuleBase {

  public AndroidInternalPhotosSyncModule(Context context, Localization localization, IPermissionsManager permissionsManager, IThreadPool threadPool, IFileStorageService fileStorageService) {
    super(context, localization, permissionsManager, threadPool, fileStorageService);
  }


  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.AndroidInternalPhotos.getTypeName();
  }


  @Override
  protected Uri getContentUri() {
    return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    return MediaStore.Images.Media.INTERNAL_CONTENT_URI; // content://media/internal/images/media
  }

  @Override
  public String getRootFolder() {
    // TODO: how to get internal pictures root path?
    return Environment.getExternalStorageDirectory().getAbsolutePath();
  }

}
