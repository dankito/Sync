package net.dankito.sync.synchronization.modules;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.CallType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.StringUtils;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
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


  @Override
  protected AndroidSyncModuleBase createSyncModuleToTest(Context appContext, IEntityManager entityManager) {
    return new AndroidCallLogSyncModule(appContext, entityManager);
  }

  @Override
  protected void testEntity(SyncEntity entityToTest) {
    Assert.assertTrue(entityToTest instanceof CallLogSyncEntity);

    CallLogSyncEntity entity = (CallLogSyncEntity)entityToTest;

    Assert.assertNotNull(entity.getNumber());
    Assert.assertTrue(entity.getNormalizedNumber() != null || entity.getNumber().length() == 0); // for incoming unknown numbers getNumber() is an empty String and getNormalizedNumber() is null
    Assert.assertNotNull(entity.getDate());
    Assert.assertTrue(entity.getDurationInSeconds() >= 0);
    Assert.assertNotNull(entity.getType());
  }


  @Test
  public void synchronizedNewEntity() throws ParseException {
    CallLogSyncEntity entity = new CallLogSyncEntity(null);

    entity.setNumber(TEST_NUMBER);
    entity.setNormalizedNumber(TEST_NORMALIZED_NUMBER);
    entity.setDate(new Date(TEST_DATE));
    entity.setDurationInSeconds(TEST_DURATION_IN_SECONDS);
    entity.setType(TEST_CALL_TYPE);
    entity.setAssociatedContactName(TEST_ASSOCIATED_CONTACT_NAME);

    addEntityToDeleteAfterTest(entity);

    underTest.synchronizedEntityRetrieved(entity, SyncEntityState.CREATED);

    testIfEntryHasSuccessfullyBeenAdded(entity);
  }

  protected void testIfEntryHasSuccessfullyBeenAdded(CallLogSyncEntity entity) {
    Assert.assertTrue(StringUtils.isNotNullOrEmpty(entity.getLookUpKeyOnSourceDevice()));

    Cursor cursor = appContext.getContentResolver().query(
        underTest.getContentUris()[0],
        null, // Which columns to return
        CallLog.Calls._ID + " = ?",       // Which rows to return (all rows)
        new String[] { entity.getLookUpKeyOnSourceDevice() },       // Selection arguments (none)
        null        // Ordering
    );

    Assert.assertTrue(cursor.moveToFirst()); // does entry with this look up key exist?

    Assert.assertEquals(TEST_NUMBER, underTest.readString(cursor, CallLog.Calls.NUMBER));
    Assert.assertEquals(TEST_NORMALIZED_NUMBER, underTest.readString(cursor, CallLog.Calls.CACHED_NORMALIZED_NUMBER));
    Assert.assertEquals(TEST_NORMALIZED_NUMBER, underTest.readString(cursor, CallLog.Calls.CACHED_FORMATTED_NUMBER));

    Assert.assertEquals(TEST_DATE, underTest.readLong(cursor, CallLog.Calls.DATE));
    Assert.assertEquals(TEST_DURATION_IN_SECONDS, underTest.readInteger(cursor, CallLog.Calls.DURATION));
    Assert.assertEquals(TEST_ASSOCIATED_CONTACT_NAME, underTest.readString(cursor, CallLog.Calls.CACHED_NAME));
  }

}
