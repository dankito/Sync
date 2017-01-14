package net.dankito.sync.synchronization;


import android.content.Context;

import net.dankito.sync.LocalConfig;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.modules.AndroidCallLogSyncModule;
import net.dankito.sync.synchronization.modules.AndroidContactsSyncModule;
import net.dankito.sync.synchronization.modules.AndroidPhotosSyncModule;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import java.util.HashMap;
import java.util.Map;

public class SyncConfigurationManagerAndroid extends SyncConfigurationManagerBase {

  protected Context context;


  public SyncConfigurationManagerAndroid(Context context, ISyncManager syncManager, IEntityManager entityManager, IDevicesManager devicesManager, IFileStorageService fileStorageService,
                                         IThreadPool threadPool, LocalConfig localConfig) {
    super(syncManager, entityManager, devicesManager, fileStorageService, threadPool, localConfig);

    this.context = context;
  }


  @Override
  protected Map<String, ISyncModule> retrieveAvailableSyncModules() {
    Map<String, ISyncModule> availableSyncModules = new HashMap<>();

    availableSyncModules.put(AndroidPhotosSyncModule.class.getName(), new AndroidPhotosSyncModule(context, entityManager, threadPool, fileStorageService));
    availableSyncModules.put(AndroidContactsSyncModule.class.getName(), new AndroidContactsSyncModule(context, entityManager, threadPool));
    availableSyncModules.put(AndroidCallLogSyncModule.class.getName(), new AndroidCallLogSyncModule(context, entityManager, threadPool));

    return availableSyncModules;
  }
}
