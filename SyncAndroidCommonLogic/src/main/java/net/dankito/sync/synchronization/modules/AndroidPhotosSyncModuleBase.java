package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.android.common.R;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

public abstract class AndroidPhotosSyncModuleBase extends AndroidFileSyncModuleBase implements ISyncModule, IFileSyncModule {



  public AndroidPhotosSyncModuleBase(Context context, Localization localization, IPermissionsManager permissionsManager, IThreadPool threadPool, FileSyncService fileSyncService, IFileStorageService fileStorageService) {
    super(context, localization, permissionsManager, threadPool, fileSyncService, fileStorageService);
  }


  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.android_photos";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_HIGHEST;
  }

  @Override
  public int getPermissionRationaleResourceId() {
    return R.string.rational_for_accessing_external_storage_permission;
  }


  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor) {
    ImageFileSyncEntity entity = new ImageFileSyncEntity();

    entity.setLocalLookupKey(readString(cursor, MediaStore.Images.Media._ID));
    entity.setCreatedOnDevice(readDate(cursor, MediaStore.Images.Media.DATE_ADDED));
    entity.setLastModifiedOnDevice(readDate(cursor, MediaStore.Images.Media.DATE_MODIFIED));
    entity.setFilePath(readString(cursor, MediaStore.Images.Media.DATA));
    entity.setFileSize(readLong(cursor, MediaStore.Images.Media.SIZE));
    entity.setMimeType(readString(cursor, MediaStore.Images.Media.MIME_TYPE));
    entity.setName(readString(cursor, MediaStore.Images.Media.DISPLAY_NAME));
    entity.setDescription(readString(cursor, MediaStore.Images.Media.DESCRIPTION));
    entity.setHeight(readInteger(cursor, MediaStore.Images.Media.HEIGHT));
    entity.setWidth(readInteger(cursor, MediaStore.Images.Media.WIDTH));
    entity.setLatitude(readDouble(cursor, MediaStore.Images.Media.LATITUDE));
    entity.setLongitude(readDouble(cursor, MediaStore.Images.Media.LONGITUDE));
    entity.setImageTakenOn(readDate(cursor, MediaStore.Images.Media.DATE_TAKEN));
    entity.setOrientation(readInteger(cursor, MediaStore.Images.Media.ORIENTATION)); //  Only degrees 0, 90, 180, 270 will work.

    entity.setLocalLookupKey(entity.getFilePath());

    return entity;
  }

}
