package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;

import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.CallType;
import net.dankito.sync.ISyncModule;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.persistence.IEntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidCallLogSyncModule extends AndroidSyncModuleBase implements ISyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidCallLogSyncModule.class);


  public AndroidCallLogSyncModule(Context context, IEntityManager entityManager) {
    super(context, entityManager);
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { CallLog.Calls.CONTENT_URI };
  }

  @Override
  protected SyncEntity mapDatabaseEntryToSyncEntity(Cursor cursor, SyncModuleConfiguration syncModuleConfiguration) {
    CallLogSyncEntity entity = new CallLogSyncEntity(syncModuleConfiguration);

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
      String associatedContactUri = readString(cursor, CallLog.Calls.CACHED_LOOKUP_URI);
    }

    entity.setDate(readDate(cursor, CallLog.Calls.DATE));
    entity.setDurationInSeconds(readInteger(cursor, CallLog.Calls.DURATION));
    entity.setType(parseCallType(cursor));

    return entity;
  }


  @Override
  protected boolean addEntityToLocalDatabase(SyncEntity synchronizedEntity) {
    return false;
  }

  @Override
  protected boolean updateEntityInLocalDatabase(SyncEntity synchronizedEntity) {
    // TODO
    return false;
  }

  @Override
  protected boolean deleteEntityFromLocalDatabase(SyncEntity entity) {
    return false;
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
