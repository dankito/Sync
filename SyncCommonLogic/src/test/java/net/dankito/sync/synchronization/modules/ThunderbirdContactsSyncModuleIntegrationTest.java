package net.dankito.sync.synchronization.modules;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.OsType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.ObjectHolder;
import net.dankito.utils.ThreadPool;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;


/**
 * These tests are not really meant to be run in CI or that like.
 * They have been used to test Thunderbird Plugin's functionality.
 *
 * They assume that there's a Thunderbird with installed Sync Plugin running and listening on Port 32797,
 * some required manual user interaction.
 */
@Ignore
public class ThunderbirdContactsSyncModuleIntegrationTest {

  protected static final int THUNDERBIRD_MESSAGES_PORT = 32797;


  protected ISyncModule underTest;

  protected DiscoveredDevice thunderbird;


  @Before
  public void setUp() throws Exception {
    Device thunderbirdInfo = new Device("Thunderbird", "Thunderbird", "Thunderbird", OsType.THUNDERBIRD, "", "45.1.7", "");
    thunderbird = new DiscoveredDevice(thunderbirdInfo, "127.0.0.1");
    thunderbird.setMessagesPort(THUNDERBIRD_MESSAGES_PORT);

    underTest = new ThunderbirdContactsSyncModule(thunderbird, mock(Localization.class), new ThreadPool());
  }


  @Test
  public void readAllEntitiesAsync() throws Exception {
    final ObjectHolder<List<ContactSyncEntity>> resultHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(List<? extends SyncEntity> entities) {
        resultHolder.setObject((List<ContactSyncEntity>)entities);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(10, TimeUnit.SECONDS); } catch(Exception ignored) { }


    assertThat(resultHolder.isObjectSet(), is(true));
    assertThat(resultHolder.getObject().size(), is(not(0)));
  }

  @Test
  public void contactIsChangedInThunderbird_SyncEntityChangeListenerGetsCalled() {
    final ObjectHolder<SyncEntityChange> resultHolder = new ObjectHolder<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    // must be manually triggered in Thunderbird by adding, changing or deleting a contact
    underTest.addSyncEntityChangeListener(new SyncEntityChangeListener() {
      @Override
      public void entityChanged(SyncEntityChange syncEntityChange) {
        resultHolder.setObject(syncEntityChange);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(1, TimeUnit.MINUTES); } catch(Exception ignored) { }


    assertThat(resultHolder.isObjectSet(), is(true));
    assertThat(resultHolder.getObject().getSyncEntity(), notNullValue());
    assertThat(resultHolder.getObject().getSyncModule(), is(underTest));
  }

}