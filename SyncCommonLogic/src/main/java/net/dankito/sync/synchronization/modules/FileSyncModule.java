package net.dankito.sync.synchronization.modules;


import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.synchronization.SyncEntityChangeListener;
import net.dankito.utils.services.IFileStorageService;

import java.io.File;
import java.util.ArrayList;

public class FileSyncModule extends SyncModuleBase implements ISyncModule {

  protected FileHandler fileHandler;


  public FileSyncModule(IFileStorageService fileStorageService) {
    this.fileHandler = new FileHandler(fileStorageService);
  }


  @Override
  public String[] getSyncEntityTypesItCanHandle() {
    return new String[] { SyncModuleDefaultTypes.AndroidPhotos.getTypeName() };
  }

  @Override
  public void readAllEntitiesAsync(ReadEntitiesCallback callback) { // TODO: here's the SyncModuleConfiguration missing as we don't know which directory to read files from
    callback.done(new ArrayList<SyncEntity>());
  }

  @Override
  public boolean synchronizedEntityRetrieved(SyncJobItem jobItem, SyncEntityState entityState) {
    if(entityState == SyncEntityState.CREATED) {
      return saveCreatedEntity(jobItem);
    }
    else if(entityState == SyncEntityState.UPDATED) {

    }
    else if(entityState == SyncEntityState.DELETED) {
      // TODO: what about bidirectional sync modules: entities deleted on destination won't in this way deleted from source
      if(jobItem.getSyncModuleConfiguration().isKeepDeletedEntitiesOnDestination()) {
        return true;
      }
      else {

      }
    }

    return false;
  }


  protected boolean saveCreatedEntity(SyncJobItem jobItem) {
    FileSyncEntity entity = (FileSyncEntity)jobItem.getEntity();
    SyncModuleConfiguration syncModuleConfiguration = jobItem.getSyncModuleConfiguration();

    File fileDestinationPath = fileHandler.getFileDestinationPath(syncModuleConfiguration.getSourcePath(), syncModuleConfiguration.getDestinationPath(), entity.getFilePath());

    return fileHandler.writeFileToDestinationPath(jobItem, fileDestinationPath);
  }


  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
    // currently is no FileSystemWatcher implemented
  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
    // currently is no FileSystemWatcher implemented
  }

}
