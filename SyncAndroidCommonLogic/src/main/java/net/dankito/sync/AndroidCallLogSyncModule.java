package net.dankito.sync;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidCallLogSyncModule extends AndroidSyncModuleBase implements ISyncModule {


  public AndroidCallLogSyncModule(Context context) {
    super(context);
  }


  @Override
  protected Uri[] getContentUris() {
    return new Uri[] { CallLog.Calls.CONTENT_URI };
  }

  @Override
  protected SyncEntity deserializeDatabaseEntry(Cursor cursor, SyncModuleConfiguration syncModuleConfiguration) {
    CallLogSyncEntity entity = new CallLogSyncEntity(syncModuleConfiguration);

    entity.setIdOnSourceDevice(readString(cursor, CallLog.Calls._ID));
    entity.setCreatedOn(readDate(cursor, CallLog.Calls.DATE));
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && readLong(cursor, CallLog.Calls.LAST_MODIFIED) > 0) {
      entity.setModifiedOn(readDate(cursor, CallLog.Calls.LAST_MODIFIED));
    }
    else{
      entity.setModifiedOn(readDate(cursor, CallLog.Calls.DATE));
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

  protected CallType parseCallType(Cursor cursor) {
    int typeInt = readInteger(cursor, CallLog.Calls.TYPE);

    switch(typeInt) {
      case CallLog.Calls.INCOMING_TYPE:
        return CallType.INCOMING;
      case CallLog.Calls.OUTGOING_TYPE:
        return CallType.OUTGOING;
      case CallLog.Calls.MISSED_TYPE:
        return CallType.MISSED;
      case CallLog.Calls.VOICEMAIL_TYPE:
        return CallType.VOICE_MAIL;
      case CallLog.Calls.REJECTED_TYPE:
        return CallType.REJECTED;
      case CallLog.Calls.BLOCKED_TYPE:
        return CallType.BLOCKED;
      case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
        return CallType.ANSWERED_EXTERNALLY;
      default:
        return CallType.UNKNOWN;
    }
  }

}
