package net.dankito.sync.synchronization;

import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.modules.SyncConfigurationChanges;

import java.util.List;


public interface ISyncConfigurationManager {

  List<ISyncModule> getAvailableSyncModules();

  ISyncModule getSyncModuleForSyncModuleConfiguration(SyncModuleConfiguration syncModuleConfiguration);

  void syncConfigurationHasBeenUpdated(SyncConfiguration syncConfiguration, SyncConfigurationChanges changes);

}
