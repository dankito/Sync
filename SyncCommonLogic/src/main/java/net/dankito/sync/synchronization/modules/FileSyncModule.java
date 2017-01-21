package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.services.IFileStorageService;

import java.util.ArrayList;

public abstract class FileSyncModule extends SyncModuleBase implements ISyncModule, IFileSyncModule {

  protected Localization localization;

  protected FileHandler fileHandler;


  public FileSyncModule(Localization localization, IFileStorageService fileStorageService) {
    super(localization);

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
  }

  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    boolean isSuccessful = false;

    if(entityState == SyncEntityState.CREATED) {
      isSuccessful = createOrUpdateFile(jobItem);
    }
    else if(entityState == SyncEntityState.UPDATED) {
      isSuccessful = createOrUpdateFile(jobItem); // TODO: first check if file data really got updated (or only file metadata)
    }
    else if(entityState == SyncEntityState.DELETED) {
      // TODO: what about bidirectional sync modules: entities deleted on destination won't in this way deleted from source
      if(jobItem.getSyncModuleConfiguration().isKeepDeletedEntitiesOnDestination()) {
        callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, true));
      }
      else {
        isSuccessful = fileHandler.deleteFile(jobItem);
      }
    }

    callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, isSuccessful));
  }


  protected boolean createOrUpdateFile(SyncJobItem jobItem) {
    return fileHandler.writeFileToDestinationPath(jobItem);
  }


  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
    // currently is no FileSystemWatcher implemented
  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
    // currently is no FileSystemWatcher implemented
  }


  @Override
  public String getRootFolder() {
    return null;
  }

}
