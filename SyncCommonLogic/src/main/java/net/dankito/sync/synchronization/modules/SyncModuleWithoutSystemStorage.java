package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.SyncEntityChangeListener;

import java.util.ArrayList;

/**
 * JavaFX Application has for Contacts, CallLog, ... no system storage as Android has
 * -> synchronizedEntityRetrieved() simply returns true as when SyncEntity reached destination device synchronization was successful.
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
    callback.done(new ArrayList<SyncEntity>());
  }

  @Override
  public boolean synchronizedEntityRetrieved(SyncJobItem jobItem, SyncEntityState entityState) {
    return true;
  }

  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {

  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {

  }

}
