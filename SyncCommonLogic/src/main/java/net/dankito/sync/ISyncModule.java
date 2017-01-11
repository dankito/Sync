package net.dankito.sync;

import net.dankito.sync.synchronization.SyncEntityChangeListener;

/**
 * Created by ganymed on 05/01/17.
 */

public interface ISyncModule {

  void readAllEntitiesAsync(SyncModuleConfiguration syncModuleConfiguration, ReadEntitiesCallback callback);

  boolean synchronizedEntityRetrieved(SyncEntity synchronizedEntity, SyncEntityState entityState);

  void addSyncEntityChangeListener(SyncEntityChangeListener listener);
  void removeSyncEntityChangeListener(SyncEntityChangeListener listener);

}
