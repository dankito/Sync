package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;


public class AndroidExternalPhotosSyncModule extends AndroidPhotosSyncModuleBase {

  public AndroidExternalPhotosSyncModule(Context context, IThreadPool threadPool, IFileStorageService fileStorageService) {
    super(context, threadPool, fileStorageService);
  }


  public String[] getSyncEntityTypesItCanHandle() {
    return new String[] { SyncModuleDefaultTypes.AndroidExternalPhotos.getTypeName() };
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { MediaStore.Images.Media.INTERNAL_CONTENT_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI };
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    // TODO: register a ContentObserver for both
//    return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  }

  @Override
  public String getRootFolder() {
    return Environment.getExternalStorageDirectory().getAbsolutePath();
  }

}
