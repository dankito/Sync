package net.dankito.sync.synchronization.modules;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import net.dankito.sync.ISyncModule;
import net.dankito.sync.ReadEntitiesCallback;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 05/01/17.
 */

public abstract class AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidSyncModuleBase.class);


  protected Context context;

  protected IEntityManager entityManager;

  protected List<SyncEntityChangeListener> syncEntityChangeListeners = new CopyOnWriteArrayList<>();


  public AndroidSyncModuleBase(Context context, IEntityManager entityManager) {
    this.context = context;
    this.entityManager = entityManager;
  }


  protected abstract Uri[] getContentUris();

  protected abstract Uri getContentUriForContentObserver();

  protected abstract boolean addEntityToLocalDatabase(SyncEntity synchronizedEntity);

  protected abstract boolean updateEntityInLocalDatabase(SyncEntity synchronizedEntity);


  /**
   * May be overwritten in sub class to return only a specific amount of columns.
   * @return
   */
  protected String[] getColumnNamesToRetrieve() {
    return null;
  }

  protected abstract SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor, SyncModuleConfiguration syncModuleConfiguration);

  public void readAllEntitiesAsync(SyncModuleConfiguration syncModuleConfiguration, ReadEntitiesCallback callback) {
    List<SyncEntity> result = new ArrayList<>();

    for(Uri contentUri : getContentUris()) {
      readEntitiesFromAndroidDatabase(result, syncModuleConfiguration, contentUri);
    }

    callback.done(result);
  }

  protected void readEntitiesFromAndroidDatabase(List<SyncEntity> result, SyncModuleConfiguration syncModuleConfiguration, Uri contentUri) {
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
        SyncEntity entity = mapDatabaseEntryToSyncEntity(cursor, syncModuleConfiguration);
        if(entity != null) {
          result.add(entity);
        }
      } while (cursor.moveToNext());
    }

    cursor.close();
  }


  @Override
  public boolean synchronizedEntityRetrieved(SyncEntity synchronizedEntity, SyncEntityState entityState) {
    if(entityState == SyncEntityState.CREATED) {
      return addEntityToLocalDatabase(synchronizedEntity);
    }
    else if(entityState == SyncEntityState.UPDATED) {
      return updateEntityInLocalDatabase(synchronizedEntity);
    }
    else if(entityState == SyncEntityState.DELETED) {
      return deleteEntityFromLocalDatabase(synchronizedEntity);
    }

    return false;
  }

  protected boolean deleteEntityFromLocalDatabase(SyncEntity entity) {
    if(StringUtils.isNotNullOrEmpty(entity.getLookUpKeyOnSourceDevice())) {
      try {
        ContentResolver resolver = context.getContentResolver();
        // Unbelievable, Motorola and HTC do not support deleting entries from call log: http://android-developers.narkive.com/W63HuY7c/delete-call-log-entry-exception
        int result = resolver.delete(Uri.withAppendedPath(getContentUris()[0], entity.getLookUpKeyOnSourceDevice()), "", null);

        return result > 0;
      } catch(Exception e) {
        log.error("Could not delete Entry from Database: " + entity, e);
      }
    }

    return false;
  }


  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
    synchronized(syncEntityChangeListeners) {
      if(syncEntityChangeListeners.size() == 0) {
        registerContentObserver();
      }

      syncEntityChangeListeners.add(listener);
    }
  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
    synchronized(syncEntityChangeListeners) {
      syncEntityChangeListeners.remove(listener);

      if(syncEntityChangeListeners.size() == 0) {
        unregisterContentObserver();
      }
    }
  }

  protected void callEntityChangedListeners(SyncEntity entity) {
    for(SyncEntityChangeListener listener : syncEntityChangeListeners) {
      listener.entityChanged(new SyncEntityChange(this, entity));
    }
  }


  protected void registerContentObserver() {
    context.getContentResolver().registerContentObserver(getContentUriForContentObserver(), true, syncEntityContentObserver);
  }

  protected void unregisterContentObserver() {
    context.getContentResolver().unregisterContentObserver(syncEntityContentObserver);
  }

  protected ContentObserver syncEntityContentObserver = new ContentObserver(null) {
    @Override
    public void onChange(boolean selfChange, Uri uri) {
      if(selfChange == false) {
        entitiesToSynchronizeChanged(uri);
      }

      super.onChange(selfChange, uri);
    }
  };

  protected void entitiesToSynchronizeChanged(Uri uri) {
    // TODO: get changed entities

    callEntityChangedListeners(null);
  }


  protected boolean wasInsertSuccessful(Uri contentUri) {
    if(contentUri != null) {
      long newEntryId = ContentUris.parseId(contentUri);

      return newEntryId >= 0;
    }

    return false;
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
