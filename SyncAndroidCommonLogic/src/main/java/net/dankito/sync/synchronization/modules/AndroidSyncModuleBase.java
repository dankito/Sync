package net.dankito.sync.synchronization.modules;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.PermissionRequestCallback;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class AndroidSyncModuleBase extends SyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidSyncModuleBase.class);


  protected Context context;

  protected IPermissionsManager permissionsManager;

  protected IThreadPool threadPool;

  protected List<SyncEntityChangeListener> syncEntityChangeListeners = new CopyOnWriteArrayList<>();


  public AndroidSyncModuleBase(Context context, IPermissionsManager permissionsManager, IThreadPool threadPool) {
    this.context = context;
    this.permissionsManager = permissionsManager;
    this.threadPool = threadPool;
  }


  protected abstract Uri getContentUri();

  protected abstract Uri getContentUriForContentObserver();

  protected abstract String getPermissionToReadEntities();

  protected abstract String getPermissionToWriteEntities();

  protected abstract int getPermissionRationaleResourceId();

  protected abstract boolean addEntityToLocalDatabase(SyncJobItem jobItem);

  protected abstract boolean updateEntityInLocalDatabase(SyncJobItem jobItem);


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
  }

  protected void readAllEntities(final ReadEntitiesCallback callback) {
    permissionsManager.checkPermission(getPermissionToReadEntities(), getPermissionRationaleResourceId(), new PermissionRequestCallback() {
      @Override
      public void permissionCheckDone(String permission, boolean isGranted) {
        if(isGranted) {
          readAllEntitiesPermissionsGranted(callback);
        }
        else {
          log.error("User didn't give Permission " + getPermissionToReadEntities() + ", cannot read entities therefore");
          callback.done(new ArrayList<SyncEntity>());
        }
      }
    });
  }

  protected void readAllEntitiesPermissionsGranted(ReadEntitiesCallback callback) {
    List<SyncEntity> result = new ArrayList<>();

    readEntitiesFromAndroidDatabase(result, getContentUri());

    callback.done(result);
  }

  protected void readEntitiesFromAndroidDatabase(List<SyncEntity> result, Uri contentUri) {
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
  public boolean synchronizedEntityRetrieved(SyncJobItem jobItem, SyncEntityState entityState) {
    // TODO: find better architecture. Yes, permission has been requested before, when reading all entities, but we should re-request it here
    if(permissionsManager.isPermissionGranted(getPermissionToWriteEntities())) {
      return synchronizedEntityRetrievedPermissionGranted(jobItem, entityState);
    }

    return false;
  }

  protected boolean synchronizedEntityRetrievedPermissionGranted(SyncJobItem jobItem, SyncEntityState entityState) {
    if(entityState == SyncEntityState.CREATED) {
      return addEntityToLocalDatabase(jobItem);
    }
    else if(entityState == SyncEntityState.UPDATED) {
      return updateEntityInLocalDatabase(jobItem);
    }
    else if(entityState == SyncEntityState.DELETED) {
      // TODO: what about bidirectional sync modules: entities deleted on destination won't in this way deleted from source
      if(jobItem.getSyncModuleConfiguration().isKeepDeletedEntitiesOnDestination() == false) {
        return deleteEntityFromLocalDatabase(jobItem);
      }
      else { // keepDeletedEntitiesOnDestination is set to true -> keep file -> synchronization is successfully done
        return true;
      }
    }

    return false;
  }

  protected boolean deleteEntityFromLocalDatabase(SyncJobItem jobItem) {
    String lookupKey = jobItem.getEntity().getLookUpKeyOnSourceDevice();

    if(StringUtils.isNotNullOrEmpty(lookupKey)) {
      try {
        ContentResolver resolver = context.getContentResolver();
        // Unbelievable, Motorola and HTC do not support deleting entries from call log: http://android-developers.narkive.com/W63HuY7c/delete-call-log-entry-exception
        int result = resolver.delete(Uri.withAppendedPath(getContentUri(), lookupKey), "", null);

        return result > 0;
      } catch(Exception e) {
        log.error("Could not delete Entry from Database: " + jobItem.getEntity(), e);
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
