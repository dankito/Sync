package net.dankito.sync.synchronization.modules;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.support.annotation.NonNull;

import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.CallType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidCallLogSyncModule extends AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidCallLogSyncModule.class);


  public AndroidCallLogSyncModule(Context context, IEntityManager entityManager, IThreadPool threadPool) {
    super(context, entityManager, threadPool);
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { CallLog.Calls.CONTENT_URI };
  }

  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor) {
    CallLogSyncEntity entity = new CallLogSyncEntity();

    entity.setLookUpKeyOnSourceDevice(readString(cursor, CallLog.Calls._ID));
    entity.setCreatedOnDevice(readDate(cursor, CallLog.Calls.DATE));
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && readLong(cursor, CallLog.Calls.LAST_MODIFIED) > 0) {
      entity.setLastModifiedOnDevice(readDate(cursor, CallLog.Calls.LAST_MODIFIED));
    }
    else{
      entity.setLastModifiedOnDevice(readDate(cursor, CallLog.Calls.DATE));
    }

    entity.setNumber(readString(cursor, CallLog.Calls.NUMBER));
    entity.setNormalizedNumber(readString(cursor, CallLog.Calls.CACHED_NORMALIZED_NUMBER));
    if(entity.getNormalizedNumber() == null) {
      entity.setNormalizedNumber(readString(cursor, CallLog.Calls.CACHED_FORMATTED_NUMBER));
    }

    entity.setAssociatedContactName(readString(cursor, CallLog.Calls.CACHED_NAME));
    if(entity.getAssociatedContactName() != null) {
      entity.setAssociatedContactLookUpKey(readString(cursor, CallLog.Calls.CACHED_LOOKUP_URI));
    }

    entity.setDate(readDate(cursor, CallLog.Calls.DATE));
    entity.setDurationInSeconds(readInteger(cursor, CallLog.Calls.DURATION));
    entity.setType(parseCallType(cursor));

    return entity;
  }


  @Override
  protected boolean addEntityToLocalDatabase(SyncJobItem jobItem) {
    try {
      CallLogSyncEntity entity = (CallLogSyncEntity)jobItem.getEntity();

      ContentValues values = mapEntityToContentValues(entity);

      Uri uri = context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
      if(uri != null) {
        long newCallLogEntryId = ContentUris.parseId(uri);
        entity.setLookUpKeyOnSourceDevice("" + newCallLogEntryId);

        return newCallLogEntryId >= 0;
      }
    } catch(Exception e) {
      log.error("Could not insert CallLogSyncEntity into Android CallLog Database: " + jobItem.getEntity(), e);
    }

    return false;
  }

  @Override
  protected boolean updateEntityInLocalDatabase(SyncJobItem jobItem) {
    try {
      CallLogSyncEntity entity = (CallLogSyncEntity)jobItem.getEntity();

      ContentValues values = mapEntityToContentValues(entity);

      Uri contentUri = Uri.withAppendedPath(CallLog.Calls.CONTENT_URI, entity.getLookUpKeyOnSourceDevice());
      int result = context.getContentResolver().update(contentUri, values, null, null);

      return result > 0;
    } catch(Exception e) {
      log.error("Could not insert CallLogSyncEntity into Android CallLog Database: " + jobItem.getEntity(), e);
    }

    return false;
  }

  @Override
  protected Uri getContentUriForContentObserver() {
    return CallLog.Calls.CONTENT_URI;
  }


  @NonNull
  protected ContentValues mapEntityToContentValues(CallLogSyncEntity entity) {
    ContentValues values = new ContentValues();

    values.put(CallLog.Calls.NUMBER, entity.getNumber());
    if(StringUtils.isNotNullOrEmpty(entity.getNormalizedNumber())) {
      values.put(CallLog.Calls.CACHED_NORMALIZED_NUMBER, entity.getNormalizedNumber());
      values.put(CallLog.Calls.CACHED_FORMATTED_NUMBER, entity.getNormalizedNumber());
    }

    if(entity.getDate() != null) {
      values.put(CallLog.Calls.DATE, entity.getDate().getTime());
    }
    values.put(CallLog.Calls.DURATION, entity.getDurationInSeconds());

    values.put(CallLog.Calls.TYPE, mapCallTypeToAndroidCallType(entity.getType()));
    values.put(CallLog.Calls.NEW, 1);

    values.put(CallLog.Calls.CACHED_NAME, entity.getAssociatedContactName());
    // TODO: is it that senseful to add Contact Lookup Uri? As on other Android system this uri has no meaning
    values.put(CallLog.Calls.CACHED_LOOKUP_URI, entity.getAssociatedContactLookUpKey());

    return values;
  }

  protected int mapCallTypeToAndroidCallType(CallType type) {
    switch(type) {
      case INCOMING:
        return CallLog.Calls.INCOMING_TYPE;
      case OUTGOING:
        return CallLog.Calls.OUTGOING_TYPE;
      case MISSED:
        return CallLog.Calls.MISSED_TYPE;
      case REJECTED:
        return CallLog.Calls.REJECTED_TYPE;
      case BLOCKED:
        return CallLog.Calls.BLOCKED_TYPE;
      case VOICE_MAIL:
        return CallLog.Calls.VOICEMAIL_TYPE;
      case ANSWERED_EXTERNALLY:
        return CallLog.Calls.ANSWERED_EXTERNALLY_TYPE;
      default:
        return 0;
    }
  }

  protected CallType parseCallType(Cursor cursor) {
    int typeInt = readInteger(cursor, CallLog.Calls.TYPE);

    switch(typeInt) {
      case CallLog.Calls.INCOMING_TYPE:
        return CallType.INCOMING;
      case CallLog.Calls.OUTGOING_TYPE:
        return CallType.OUTGOING;
      case CallLog.Calls.MISSED_TYPE:
        return CallType.MISSED;
      case CallLog.Calls.REJECTED_TYPE:
        return CallType.REJECTED;
      case CallLog.Calls.BLOCKED_TYPE:
        return CallType.BLOCKED;
      case CallLog.Calls.VOICEMAIL_TYPE:
        return CallType.VOICE_MAIL;
      case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
        return CallType.ANSWERED_EXTERNALLY;
      default:
        return CallType.UNKNOWN;
    }
  }

}
