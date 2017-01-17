package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.annotation.NonNull;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.CallType;
import net.dankito.sync.SyncEntity;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;

import org.junit.Assert;

import java.util.Date;

/**
 * Created by ganymed on 05/01/17.
 */

public class AndroidCallLogSyncModuleTest extends AndroidSyncModuleTestBase {

  protected static final String TEST_NUMBER = "012345678901";
  protected static final String TEST_NORMALIZED_NUMBER = "+4712345678901";
  protected static final long TEST_DATE = new Date().getTime();
  protected static final int TEST_DURATION_IN_SECONDS = 11 * 60 + 23;
  protected static final CallType TEST_CALL_TYPE = CallType.OUTGOING;
  protected static final String TEST_ASSOCIATED_CONTACT_NAME = "Theresa";
  protected static final String TEST_ASSOCIATED_CONTACT_LOOKUP_KEY = "content://com.android.contacts/contacts/lookup/3585r1-2B314331474B3953294F/1";

  protected static final String TEST_UPDATED_NUMBER = "012345678902";
  protected static final String TEST_UPDATED_NORMALIZED_NUMBER = "+4712345678902";
  protected static final long TEST_UPDATED_DATE = new Date().getTime() + 1;
  protected static final int TEST_UPDATED_DURATION_IN_SECONDS = 11 * 60 + 24;
  protected static final CallType TEST_UPDATED_CALL_TYPE = CallType.INCOMING;
  protected static final String TEST_UPDATED_ASSOCIATED_CONTACT_NAME = "Mother Theresa";
  protected static final String TEST_UPDATED_ASSOCIATED_CONTACT_LOOKUP_KEY = "content://com.android.contacts/contacts/lookup/3585r6-3D3F29514D/6";


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context context, IPermissionsManager permissionsManager, IThreadPool threadPool) {
    return new AndroidCallLogSyncModule(context, permissionsManager, threadPool);
  }

  @Override
  protected void testReadEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof CallLogSyncEntity);

    CallLogSyncEntity entity = (CallLogSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getNumber());
    Assert.assertTrue(entity.getNormalizedNumber() != null || entity.getNumber().length() == 0); // for incoming unknown numbers getNumber() is an empty String and getNormalizedNumber() is null
    Assert.assertNotNull(entity.getDate());
    Assert.assertTrue(entity.getDurationInSeconds() >= 0);
    Assert.assertNotNull(entity.getType());
  }


  @Override
  protected void testIfEntryHasSuccessfullyBeenAdded(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLocalLookupKey()));

    Cursor cursor = getCursorForEntity(entity);

    Assert.assertTrue(cursor.moveToFirst()); // does entry with this look up key exist?

    Assert.assertEquals(TEST_NUMBER, underTest.readString(cursor, CallLog.Calls.NUMBER));
    Assert.assertEquals(TEST_NORMALIZED_NUMBER, underTest.readString(cursor, CallLog.Calls.CACHED_NORMALIZED_NUMBER));

    Assert.assertEquals(TEST_DATE, underTest.readLong(cursor, CallLog.Calls.DATE));
    Assert.assertEquals(TEST_DURATION_IN_SECONDS, underTest.readInteger(cursor, CallLog.Calls.DURATION));
    Assert.assertEquals(TEST_ASSOCIATED_CONTACT_NAME, underTest.readString(cursor, CallLog.Calls.CACHED_NAME));
    Assert.assertEquals(TEST_ASSOCIATED_CONTACT_LOOKUP_KEY, underTest.readString(cursor, CallLog.Calls.CACHED_LOOKUP_URI));
  }

  @Override
  protected void testIfEntryHasSuccessfullyBeenUpdated(SyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLocalLookupKey()));

    Cursor cursor = getCursorForEntity(entity);

    Assert.assertTrue(cursor.moveToFirst()); // does entry with this look up key exist?

    Assert.assertEquals(TEST_UPDATED_NUMBER, underTest.readString(cursor, CallLog.Calls.NUMBER));
    Assert.assertEquals(TEST_UPDATED_NORMALIZED_NUMBER, underTest.readString(cursor, CallLog.Calls.CACHED_NORMALIZED_NUMBER));

    Assert.assertEquals(TEST_UPDATED_DATE, underTest.readLong(cursor, CallLog.Calls.DATE));
    Assert.assertEquals(TEST_UPDATED_DURATION_IN_SECONDS, underTest.readInteger(cursor, CallLog.Calls.DURATION));
    Assert.assertEquals(TEST_UPDATED_ASSOCIATED_CONTACT_NAME, underTest.readString(cursor, CallLog.Calls.CACHED_NAME));
    Assert.assertEquals(TEST_UPDATED_ASSOCIATED_CONTACT_LOOKUP_KEY, underTest.readString(cursor, CallLog.Calls.CACHED_LOOKUP_URI));
  }


  @NonNull
  @Override
  protected SyncEntity createTestEntity() {
    CallLogSyncEntity entity = new CallLogSyncEntity();

    entity.setNumber(TEST_NUMBER);
    entity.setNormalizedNumber(TEST_NORMALIZED_NUMBER);
    entity.setDate(new Date(TEST_DATE));
    entity.setDurationInSeconds(TEST_DURATION_IN_SECONDS);
    entity.setType(TEST_CALL_TYPE);
    entity.setAssociatedContactName(TEST_ASSOCIATED_CONTACT_NAME);
    entity.setAssociatedContactLookupKey(TEST_ASSOCIATED_CONTACT_LOOKUP_KEY);

    return entity;
  }

  @Override
  protected void updateTestEntity(SyncEntity entityToUpdate) {
    CallLogSyncEntity entity = (CallLogSyncEntity)entityToUpdate;

    entity.setNumber(TEST_UPDATED_NUMBER);
    entity.setNormalizedNumber(TEST_UPDATED_NORMALIZED_NUMBER);
    entity.setDate(new Date(TEST_UPDATED_DATE));
    entity.setDurationInSeconds(TEST_UPDATED_DURATION_IN_SECONDS);
    entity.setType(TEST_UPDATED_CALL_TYPE);
    entity.setAssociatedContactName(TEST_UPDATED_ASSOCIATED_CONTACT_NAME);
    entity.setAssociatedContactLookupKey(TEST_UPDATED_ASSOCIATED_CONTACT_LOOKUP_KEY);
  }

  @NonNull
  protected String getIdColumnForEntity() {
    return CallLog.Calls._ID;
  }

}
