package net.dankito.sync;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidImageSyncModule extends AndroidSyncModuleBase implements ISyncModule {


  public AndroidImageSyncModule(Context context) {
    super(context);
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { MediaStore.Images.Media.INTERNAL_CONTENT_URI, MediaStore.Images.Media.EXTERNAL_CONTENT_URI };
  }

  @Override
  protected Entity deserializeDatabaseEntry(Cursor cursor) {
    ImageFileEntity entity = new ImageFileEntity();

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
    entity.setImageTaken(readDate(cursor, MediaStore.Images.Media.DATE_TAKEN));
    entity.setOrientation(readInteger(cursor, MediaStore.Images.Media.ORIENTATION)); //  Only degrees 0, 90, 180, 270 will work.

//    Object bucketDisplayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
//    Object bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
//    Object title = readString(cursor, MediaStore.Images.Media.TITLE);

    return entity;
  }

}
