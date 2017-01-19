package net.dankito.sync.synchronization;

import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.synchronization.modules.ISyncModule;

import java.util.List;


public interface ISyncConfigurationManager {

  List<ISyncModule> getAvailableSyncModules();

  ISyncModule getSyncModuleForSyncModuleConfiguration(SyncModuleConfiguration syncModuleConfiguration);

}
