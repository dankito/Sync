package net.dankito.sync.synchronization.modules;

import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.synchronization.SyncEntityChangeListener;

/**
 * Created by ganymed on 05/01/17.
 */

public interface ISyncModule {

  String getName();

  int getDisplayPriority();

  String getSyncEntityTypeItCanHandle();

  void readAllEntitiesAsync(ReadEntitiesCallback callback);

  boolean synchronizedEntityRetrieved(SyncJobItem jobItem, SyncEntityState entityState);

  void addSyncEntityChangeListener(SyncEntityChangeListener listener);
  void removeSyncEntityChangeListener(SyncEntityChangeListener listener);

}
