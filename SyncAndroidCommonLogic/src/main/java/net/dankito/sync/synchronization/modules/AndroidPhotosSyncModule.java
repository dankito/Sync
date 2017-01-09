package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import net.dankito.sync.ISyncModule;
import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.IEntityManager;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidPhotosSyncModule extends AndroidSyncModuleBase implements ISyncModule {


  public AndroidPhotosSyncModule(Context context, IEntityManager entityManager) {
    super(context, entityManager);
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { MediaStore.Images.Media.INTERNAL_CONTENT_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI };
  }

  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor, SyncModuleConfiguration syncModuleConfiguration) {
    ImageFileSyncEntity entity = new ImageFileSyncEntity(syncModuleConfiguration);

    entity.setIdOnSourceDevice(readString(cursor, MediaStore.Images.Media._ID));
    entity.setCreatedOn(readDate(cursor, MediaStore.Images.Media.DATE_ADDED));
    entity.setModifiedOn(readDate(cursor, MediaStore.Images.Media.DATE_MODIFIED));
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

}
