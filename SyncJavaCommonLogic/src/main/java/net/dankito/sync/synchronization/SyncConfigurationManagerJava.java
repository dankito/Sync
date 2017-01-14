package net.dankito.sync.synchronization;

import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;


@Named
public class SyncConfigurationManagerJava extends SyncConfigurationManagerBase {

  public SyncConfigurationManagerJava(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IFileStorageService fileStorageService, IThreadPool threadPool) {
    super(syncManager, dataManager, entityManager, devicesManager, fileStorageService, threadPool);
  }


  @Override
  protected Map<String, ISyncModule> retrieveAvailableSyncModules() {
    return new HashMap<>();
  }

}
