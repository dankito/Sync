package net.dankito.sync.synchronization;


import android.content.Context;

import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.modules.AndroidCallLogSyncModule;
import net.dankito.sync.synchronization.modules.AndroidContactsSyncModule;
import net.dankito.sync.synchronization.modules.AndroidExternalPhotosSyncModule;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.ArrayList;
import java.util.List;

public class SyncConfigurationManagerAndroid extends SyncConfigurationManagerBase {

  protected Context context;


  public SyncConfigurationManagerAndroid(Context context, ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                         IFileStorageService fileStorageService, IThreadPool threadPool) {
    super(syncManager, dataManager, entityManager, devicesManager, fileStorageService, threadPool);

    this.context = context;
  }


  @Override
  protected List<ISyncModule> retrieveAvailableSyncModules() {
    List<ISyncModule> availableSyncModules = new ArrayList<>();

    availableSyncModules.add(new AndroidExternalPhotosSyncModule(context, threadPool, fileStorageService));
    availableSyncModules.add(new AndroidContactsSyncModule(context, threadPool));
    availableSyncModules.add(new AndroidCallLogSyncModule(context, threadPool));

    return availableSyncModules;
  }
}
