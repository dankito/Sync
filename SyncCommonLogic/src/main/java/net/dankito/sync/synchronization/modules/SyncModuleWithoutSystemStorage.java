package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.localization.Localization;

import java.util.ArrayList;

/**
 * JavaFX Application has for Contacts, CallLog, ... no system storage as Android has
 * -> handleRetrievedSynchronizedEntityAsync() simply returns true as when SyncEntity reached destination device synchronization was successful.
 */
public abstract class SyncModuleWithoutSystemStorage extends SyncModuleBase implements ISyncModule {


  public SyncModuleWithoutSystemStorage(Localization localization) {
    super(localization);
  }


  @Override
  protected String getNameStringResourceKey() {
    return null; // TODO
  }

  @Override
  public int getDisplayPriority() {
    return 0; // TODO
  }

  @Override
  public void readAllEntitiesAsync(ReadEntitiesCallback callback) {
    callback.done(false, new ArrayList<SyncEntity>());

    super.readAllEntitiesAsync(callback);
  }

  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, true));

    super.handleRetrievedSynchronizedEntityAsync(jobItem, entityState, callback); // inform linked SyncModules
  }

}
