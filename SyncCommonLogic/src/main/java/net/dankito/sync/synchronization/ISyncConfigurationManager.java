package net.dankito.sync.synchronization;

import net.dankito.sync.ISyncModule;

import java.util.List;


public interface ISyncConfigurationManager {

  List<ISyncModule> getAvailableSyncModules();

}
