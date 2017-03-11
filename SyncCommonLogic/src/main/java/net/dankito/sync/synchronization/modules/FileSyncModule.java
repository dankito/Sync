package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.files.FileSyncListener;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.sync.synchronization.files.RetrievedFile;
import net.dankito.utils.StringUtils;
import net.dankito.utils.services.IFileStorageService;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FileSyncModule extends SyncModuleBase implements ISyncModule, IFileSyncModule {

  protected Localization localization;

  protected FileSyncService fileSyncService;

  protected FileHandler fileHandler;

  protected Map<SyncJobItem, HandleRetrievedSynchronizedEntityCallback> pendingCallbacks = new ConcurrentHashMap<>();


  public FileSyncModule(Localization localization, FileSyncService fileSyncService, IFileStorageService fileStorageService) {
    super(localization);

    this.fileSyncService = fileSyncService;
    fileSyncService.addFileSyncListener(fileSyncListener);
    this.fileHandler = new FileHandler(fileStorageService);
  }


  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.files";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_LOW;
  }

  @Override
  public void readAllEntitiesAsync(ReadEntitiesCallback callback) { // TODO: here's the SyncModuleConfiguration missing as we don't know which directory to read files from
    callback.done(new ArrayList<SyncEntity>());

    super.readAllEntitiesAsync(callback);
  }

  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    if(entityState == SyncEntityState.CREATED) {
      createOrUpdateFile(jobItem, callback);
    }
    else if(entityState == SyncEntityState.CHANGED) {
      createOrUpdateFile(jobItem, callback); // TODO: first check if file data really got updated (or only file metadata)
    }
    else if(entityState == SyncEntityState.DELETED) {
      // TODO: what about bidirectional sync modules: entities deleted on destination won't in this way deleted from source
      if(jobItem.getSyncModuleConfiguration().isKeepDeletedEntitiesOnDestination()) {
        callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, true));
      }
      else {
        boolean isSuccessful = fileHandler.deleteFile(jobItem);
        callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, isSuccessful));
      }
    }

    super.handleRetrievedSynchronizedEntityAsync(jobItem, entityState, callback); // inform linked SyncModules
  }


  @Override
  public void configureLocalSynchronizationSettings(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    super.configureLocalSynchronizationSettings(remoteDevice, syncModuleConfiguration);

    if(StringUtils.isNullOrEmpty(syncModuleConfiguration.getDestinationPath())) {
      File destinationFolder = new File("data", remoteDevice.getDevice().getDeviceDisplayName() + "_" + remoteDevice.getDevice().getUniqueDeviceId());

      syncModuleConfiguration.setDestinationPath(destinationFolder.getAbsolutePath());
    }
  }

  protected void createOrUpdateFile(SyncJobItem jobItem, HandleRetrievedSynchronizedEntityCallback callback) {
    pendingCallbacks.put(jobItem, callback);

    fileSyncService.fileSyncJobItemRetrieved(jobItem);
    fileSyncService.start();
  }


  @Override
  public String getRootFolder() {
    return null;
  }


  protected FileSyncListener fileSyncListener = new FileSyncListener() {
    @Override
    public void fileRetrieved(RetrievedFile retrievedFile) {
      retrievedFile(retrievedFile);
    }
  };

  protected void retrievedFile(RetrievedFile retrievedFile) {
    HandleRetrievedSynchronizedEntityCallback callback = pendingCallbacks.remove(retrievedFile.getJobItem());
    if(callback != null) {
      callback.done(new HandleRetrievedSynchronizedEntityResult(retrievedFile.getJobItem(), true));
    }
  }

}
