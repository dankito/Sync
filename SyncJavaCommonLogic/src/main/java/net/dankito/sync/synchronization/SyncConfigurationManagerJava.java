package net.dankito.sync.synchronization;

import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.modules.FileSyncModule;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.modules.SyncModuleWithoutSystemStorage;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;


@Named
public class SyncConfigurationManagerJava extends SyncConfigurationManagerBase {

  protected Localization localization;


  public SyncConfigurationManagerJava(Localization localization, ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IDataMerger dataMerger, IFileStorageService fileStorageService, IThreadPool threadPool) {
    super(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileStorageService, threadPool);

    this.localization = localization;
  }


  @Override
  protected List<ISyncModule> retrieveAvailableSyncModules() {
    List<ISyncModule> availableSyncModules = new ArrayList<>();

    availableSyncModules.add(new FileSyncModule(localization, fileStorageService));
    availableSyncModules.add(new SyncModuleWithoutSystemStorage(localization));

    return availableSyncModules;
  }

}
