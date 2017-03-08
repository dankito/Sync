package net.dankito.sync.synchronization.modules;

import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.SyncEntityChangeListener;

/**
 * Created by ganymed on 05/01/17.
 */

public interface ISyncModule {

  String getName();

  int getDisplayPriority();

  String getSyncEntityTypeItCanHandle();

  void readAllEntitiesAsync(ReadEntitiesCallback callback);

  void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback);

  boolean deleteSyncEntityProperty(SyncEntity entity, SyncEntity property);

  void addSyncEntityChangeListener(SyncEntityChangeListener listener);
  void removeSyncEntityChangeListener(SyncEntityChangeListener listener);

  void configureLocalSynchronizationSettings(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration);

  boolean registerLinkedSyncModule(ISyncModule syncModule);

  boolean unregisterLinkedSyncModule(ISyncModule syncModule);
}
