package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;


public class AndroidExternalPhotosSyncModule extends AndroidPhotosSyncModuleBase {

  public AndroidExternalPhotosSyncModule(Context context, IPermissionsManager permissionsManager, IThreadPool threadPool, IFileStorageService fileStorageService) {
    super(context, permissionsManager, threadPool, fileStorageService);
  }


  public String[] getSyncEntityTypesItCanHandle() {
    return new String[] { SyncModuleDefaultTypes.AndroidExternalPhotos.getTypeName() };
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
