package net.dankito.sync.synchronization.modules;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.provider.MediaStore;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.Device;
import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.android.common.R;
import net.dankito.sync.localization.Localization;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class AndroidPhotosSyncModuleBase extends AndroidSyncModuleBase implements ISyncModule, IFileSyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidPhotosSyncModuleBase.class);


  protected FileHandler fileHandler;


  public AndroidPhotosSyncModuleBase(Context context, Localization localization, IPermissionsManager permissionsManager, IThreadPool threadPool, IFileStorageService fileStorageService) {
    super(context, localization, permissionsManager, threadPool);

    this.fileHandler = new FileHandler(fileStorageService);
  }

  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.photos";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_HIGHEST;
  }

  @Override
  protected String getPermissionToReadEntities() {
    return Manifest.permission.READ_EXTERNAL_STORAGE;
  }

  @Override
  protected String getPermissionToWriteEntities() {
    return Manifest.permission.WRITE_EXTERNAL_STORAGE;
  }

  @Override
  protected int getPermissionRationaleResourceId() {
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

//    Object bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
//    Object bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
//    Object title = readString(cursor, MediaStore.Images.Media.TITLE);

    return entity;
  }


  @Override
  protected boolean addEntityToLocalDatabase(SyncJobItem jobItem) {
    ImageFileSyncEntity entity = (ImageFileSyncEntity)jobItem.getEntity();

    String deviceName = getDeviceName(jobItem);
    File directory = new File(getRootFolder(), deviceName);
    String fileName = entity.getName() != null ? entity.getName() : "file_" + System.currentTimeMillis() + ".jpg";
    File fileDestinationPath = new File(directory, fileName);

    if(fileHandler.writeFileToDestinationPath(jobItem, fileDestinationPath)) {
      notifyAndroidSystemOfNewImageAsync(entity, fileDestinationPath);

      return true;
    }

    return false;
  }

  @Override
  protected boolean updateEntityInLocalDatabase(SyncJobItem jobItem) {
    // TODO
    // first get File instance for previously saved file
    // if name changed, move previously saved file
    // if syncEntityData != null, write to file
    return false;
  }

  // TODO: also implement deleting file?


  protected void updateLastModifiedDate(SyncJobItem jobItem) {
    // TODO
  }


  protected void notifyAndroidSystemOfNewImageAsync(final ImageFileSyncEntity entity, final File file) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        notifyAndroidSystemOfChangedOrAddedImage(entity, file);
      }
    });
  }

  protected void notifyAndroidSystemOfChangedOrAddedImage(ImageFileSyncEntity entity, File file) {
    try {
      MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath()}, new String[] { entity.getMimeType() }, null);
    } catch(Exception e) { log.error("Could not start MediaScanner for inserted image file " + file.getAbsolutePath(), e); }
  }

  protected String getDeviceName(SyncJobItem jobItem) {
    Device remoteDevice = jobItem.getSourceDevice();
    return remoteDevice.getDeviceDisplayName();
  }


//    Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");

}
