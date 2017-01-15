package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.synchronization.SyncEntityChangeListener;

import java.util.ArrayList;

public class FileSyncModule extends SyncModuleBase implements ISyncModule {

  @Override
  public String[] getSyncEntityTypesItCanHandle() {
    return new String[] { SyncModuleDefaultTypes.AndroidPhotos.getTypeName() };
  }

  @Override
  public void readAllEntitiesAsync(ReadEntitiesCallback callback) { // TODO: here's the SyncModuleConfiguration missing as we don't know which directory to read files from
    callback.done(new ArrayList<SyncEntity>());
  }

  @Override
  public boolean synchronizedEntityRetrieved(SyncJobItem jobItem, SyncEntityState entityState) {
    return false;
  }

  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
    // currently is no FileSystemWatcher implemented
  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
    // currently is no FileSystemWatcher implemented
  }

}
