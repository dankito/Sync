package net.dankito.sync;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ganymed on 05/01/17.
 */

public abstract class AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidSyncModuleBase.class);


  protected Context context;


  public AndroidSyncModuleBase(Context context) {
    this.context = context;
  }


  protected abstract Uri[] getContentUris();

  /**
   * May be overwritten in sub class to return only a specific amount of columns.
   * @return
   */
  protected String[] getColumnNamesToRetrieve() {
    return null;
  }

  protected abstract Entity deserializeDatabaseEntry(Cursor cursor);

  public void readAllEntitiesAsync(ReadEntitiesCallback callback) {
    List<Entity> result = new ArrayList<>();

    for(Uri contentUri : getContentUris()) {
      readEntitiesFromDatabase(result, contentUri);
    }

    callback.done(result);
  }

  protected void readEntitiesFromDatabase(List<Entity> result, Uri contentUri) {
    Cursor cursor = context.getContentResolver().query(
        contentUri,
        getColumnNamesToRetrieve(), // Which columns to return
        null,       // Which rows to return (all rows)
        null,       // Selection arguments (none)
        null        // Ordering
    );

    log.info(getClass().getSimpleName() + ": " + cursor.getCount() + " results retrieved for Uri " + contentUri);

    if(cursor.moveToFirst()) {
      do {
        Entity entity = deserializeDatabaseEntry(cursor);
        result.add(entity);
      } while (cursor.moveToNext());
    }

    cursor.close();
  }


  protected String readString(Cursor cursor, String columnName) {
    return cursor.getString(getColumnIndex(cursor, columnName));
  }

  protected int readInteger(Cursor cursor, String columnName) {
    return cursor.getInt(getColumnIndex(cursor, columnName));
  }

  protected long readLong(Cursor cursor, String columnName) {
    return cursor.getLong(getColumnIndex(cursor, columnName));
  }

  protected double readDouble(Cursor cursor, String columnName) {
    return cursor.getDouble(getColumnIndex(cursor, columnName));
  }

  protected boolean readBoolean(Cursor cursor, String columnName) {
    return readInteger(cursor, columnName) > 0;
  }

  protected Date readDate(Cursor cursor, String columnName) {
    Long secondsOrMillisecondsSince1Jan1970 = readLong(cursor, columnName);

    if(secondsOrMillisecondsSince1Jan1970 < 10000000000L) { // seconds
      return new Date(secondsOrMillisecondsSince1Jan1970 * 1000);
    }
    else { // milliseconds
      return new Date(secondsOrMillisecondsSince1Jan1970);
    }
  }

  protected int getColumnIndex(Cursor cursor, String columnName) {
    return cursor.getColumnIndex(columnName);
  }

}
