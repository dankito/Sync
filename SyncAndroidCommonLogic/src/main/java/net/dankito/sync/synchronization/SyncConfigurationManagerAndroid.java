package net.dankito.sync.synchronization;


import android.content.Context;

import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.sync.synchronization.merge.IDataMerger;
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

  protected Localization localization;

  protected FileSyncService fileSyncService;


  public SyncConfigurationManagerAndroid(Context context, Localization localization, ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                         IDataMerger dataMerger, FileSyncService fileSyncService, IFileStorageService fileStorageService, IThreadPool threadPool) {
    super(syncManager, dataManager, entityManager, devicesManager, dataMerger, fileStorageService, threadPool);

    this.context = context;
    this.localization = localization;
    this.fileSyncService = fileSyncService;
  }


  @Override
  protected List<ISyncModule> retrieveAvailableSyncModules() {
    List<ISyncModule> availableSyncModules = new ArrayList<>();

    availableSyncModules.add(new AndroidExternalPhotosSyncModule(context, localization, threadPool, fileSyncService, fileStorageService));
    availableSyncModules.add(new AndroidContactsSyncModule(context, localization, threadPool));
    availableSyncModules.add(new AndroidCallLogSyncModule(context, localization, threadPool));

    return availableSyncModules;
  }
}
