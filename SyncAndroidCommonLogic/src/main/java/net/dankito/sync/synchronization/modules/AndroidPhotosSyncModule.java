package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidPhotosSyncModule extends AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidPhotosSyncModule.class);


  protected IFileStorageService fileStorageService;


  public AndroidPhotosSyncModule(Context context, IEntityManager entityManager, IThreadPool threadPool, IFileStorageService fileStorageService) {
    super(context, entityManager, threadPool);

    this.fileStorageService = fileStorageService;
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { MediaStore.Images.Media.INTERNAL_CONTENT_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI };
  }

  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor) {
    ImageFileSyncEntity entity = new ImageFileSyncEntity();

    entity.setLookUpKeyOnSourceDevice(readString(cursor, MediaStore.Images.Media._ID));
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

    // TODO: name folder to source device (or destination device respectively)
    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "synchronized");
    directory.mkdirs();
    String fileName = entity.getName() != null ? entity.getName() : "file_" + System.currentTimeMillis() + ".jpg";
    File file = new File(directory, fileName);
    try {
      file.createNewFile();

      fileStorageService.writeToBinaryFile(jobItem.getSyncEntityData(), file.getAbsolutePath());
      entity.setLookUpKeyOnSourceDevice(file.getAbsolutePath());

      notifyAndroidSystemOfNewImageAsync(entity, file);

      return true;
    } catch (Exception e) { log.error("Could not write entity data to file for entity " + entity, e); }
    return false;
  }

  protected void notifyAndroidSystemOfNewImageAsync(final ImageFileSyncEntity entity, final File file) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        notifyAndroidSystemOfNewImage(entity, file);
      }
    });
  }

  protected void notifyAndroidSystemOfNewImage(ImageFileSyncEntity entity, File file) {
    try {
      MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath()}, new String[] { entity.getMimeType() }, null);
    } catch(Exception e) { log.error("Could not start MediaScanner for inserted image file " + file.getAbsolutePath(), e); }
  }

  @Override
  protected boolean updateEntityInLocalDatabase(SyncJobItem jobItem) {
    return false;
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    // TODO: register a ContentObserver for both
//    return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  }

//    Cursor cursor = context.getContentResolver().query(uri, null, null, null, "date_added DESC");

}
