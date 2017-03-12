package net.dankito.sync.synchronization.modules;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import net.dankito.android.util.services.PermissionsManager;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class AndroidSyncModuleBase extends SyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidSyncModuleBase.class);


  protected Context context;

  protected IThreadPool threadPool;


  public AndroidSyncModuleBase(Context context, Localization localization, IThreadPool threadPool) {
    super(localization);

    this.context = context;
    this.threadPool = threadPool;
  }


  protected abstract Uri getContentUri();

  protected abstract Uri getContentUriForContentObserver();

  public abstract String getPermissionToReadEntities();

  public abstract String getPermissionToWriteEntities();

  public abstract int getPermissionRationaleResourceId();

  protected abstract boolean addEntityToLocalDatabase(SyncJobItem jobItem);

  protected abstract boolean updateEntityInLocalDatabase(SyncJobItem jobItem);

  protected abstract void updateLastModifiedDate(SyncJobItem jobItem);


  /**
   * May be overwritten in sub class to return only a specific amount of columns.
   * @return
   */
  protected String[] getColumnNamesToRetrieve() {
    return null;
  }

  protected abstract SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor);

  public void readAllEntitiesAsync(final ReadEntitiesCallback callback) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        readAllEntities(callback);
      }
    });

    super.readAllEntitiesAsync(callback);
  }

  protected void readAllEntities(final ReadEntitiesCallback callback) {
    if(isPermissionGranted(getPermissionToReadEntities())) {
      readAllEntitiesPermissionsGranted(callback);
    }
    else {
      log.error("User didn't give Permission " + getPermissionToReadEntities() + ", cannot read entities therefore");
      callback.done(false, new ArrayList<SyncEntity>());
    }
  }

  protected void readAllEntitiesPermissionsGranted(ReadEntitiesCallback callback) {
    List<SyncEntity> result = new ArrayList<>();

    synchronized(this) { // avoid that data gets read and written to the same time
      readDataFromAndroidDatabase(result, getContentUri());
    }

    callback.done(true, result);
  }

  protected void readDataFromAndroidDatabase(List<SyncEntity> result, Uri contentUri) {
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
        SyncEntity entity = mapDatabaseEntryToSyncEntity(cursor);
        if(entity != null) {
          result.add(entity);
        }
      } while (cursor.moveToNext());
    }

    cursor.close();
  }


  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    // TODO: find better architecture. Yes, permission has been requested before, when reading all entities, but we should re-request it here
    if(isPermissionGranted(getPermissionToWriteEntities())) {
      synchronizedEntityRetrievedPermissionGranted(jobItem, entityState, callback);
    }
    else {
      log.error("Permission " + getPermissionToWriteEntities() + " is denied, cannot handle " + jobItem + " though");
      callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, false, false));
    }

    super.handleRetrievedSynchronizedEntityAsync(jobItem, entityState, callback); // inform linked SyncModules
  }

  protected void synchronizedEntityRetrievedPermissionGranted(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    boolean isSuccessful = false;

    synchronized(this) {  // avoid that data gets read and written at the same time
      if(entityState == SyncEntityState.CREATED) {
        isSuccessful = addEntityToLocalDatabase(jobItem);
      }
      else if(entityState == SyncEntityState.CHANGED) {
        isSuccessful = updateEntityInLocalDatabase(jobItem);
      }
      else if(entityState == SyncEntityState.DELETED) {
        // TODO: what about bidirectional sync modules: entities deleted on destination won't in this way deleted from source
        if(jobItem.getSyncModuleConfiguration().isKeepDeletedEntitiesOnDestination() == false) {
          isSuccessful = deleteEntityFromLocalDatabase(jobItem);
        }
        else { // keepDeletedEntitiesOnDestination is set to true -> keep file -> synchronization is successfully done
          return; // no updating lastModifiedOn
        }
      }

      updateLastModifiedDate(jobItem);
    }

    callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, isSuccessful));
  }

  protected boolean deleteEntityFromLocalDatabase(SyncJobItem jobItem) {
    String lookupKey = jobItem.getEntity().getLocalLookupKey();

    if(StringUtils.isNotNullOrEmpty(lookupKey)) {
      try {
        ContentResolver resolver = context.getContentResolver();
        int result = resolver.delete(getContentUri(), BaseColumns._ID + " = ? ", new String[] { lookupKey });

        return result > 0;
      } catch(Exception e) {
        log.error("Could not delete Entry from Database: " + jobItem.getEntity(), e);
      }
    }

    return false;
  }

  protected boolean isPermissionGranted(String permission) {
    return PermissionsManager.isPermissionGranted(context, permission);
  }


  @Override
  protected void listenerAdded(SyncEntityChangeListener addedListener, List<SyncEntityChangeListener> allListeners) {
    if(allListeners.size() == 1) { // first listener added
      registerContentObserver();
    }

    super.listenerAdded(addedListener, allListeners);
  }

  @Override
  protected void listenerRemoved(SyncEntityChangeListener removedListener, List<SyncEntityChangeListener> allListeners) {
    if(allListeners.size() == 0) {
      unregisterContentObserver();
    }

    super.listenerRemoved(removedListener, allListeners);
  }

  protected void callEntityChangedListeners() {
    callSyncEntityChangeListeners(new SyncEntityChange(this, false));
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

    callEntityChangedListeners();
  }


  protected boolean wasInsertSuccessful(Uri contentUri) {
    if(contentUri != null) {
      long newEntryId = ContentUris.parseId(contentUri);

      return newEntryId >= 0;
    }

    return false;
  }


  protected Long parseLocalLookupKeyToLong(SyncEntity entity) {
    return parseLocalLookupKeyToLong(entity.getLocalLookupKey());
  }

  protected Long parseLocalLookupKeyToLong(String localLookupKey) {
    try {
      return Long.parseLong(localLookupKey);
    } catch(Exception e) { log.error("Could not parse local lookup key " + localLookupKey + " to long", e); }

    return null;
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
