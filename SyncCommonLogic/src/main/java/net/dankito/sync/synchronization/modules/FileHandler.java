package net.dankito.sync.synchronization.modules;


import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * Implements common file handling functionality for Android and Java SyncModules.
 * As the have different parent classes, these methods cannot be implemented in a common parent class.
 */
public class FileHandler {

  private static final Logger log = LoggerFactory.getLogger(FileHandler.class);


  protected IFileStorageService fileStorageService;


  public FileHandler(IFileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }


  protected boolean deleteFile(SyncJobItem jobItem) {
    try {
      FileSyncEntity entity = (FileSyncEntity) jobItem.getEntity();
      SyncModuleConfiguration syncModuleConfiguration = jobItem.getSyncModuleConfiguration();

      File fileDestinationPath = getFileDestinationPath(syncModuleConfiguration.getSourcePath(), syncModuleConfiguration.getDestinationPath(), entity.getFilePath());

      return fileDestinationPath.delete();
    } catch(Exception e) {
      log.error("Could not delete file for SyncJobItem " + jobItem, e);
    }

    return false;
  }



  protected File getFileDestinationPath(String synchronizationSourceRootFolder, String synchronizationDestinationRootFolder, String entitySourcePathString) {
    File entityRelativeSourcePath = getFileRelativePath(synchronizationSourceRootFolder, entitySourcePathString);

    return new File(synchronizationDestinationRootFolder, entityRelativeSourcePath.getPath());
  }

  protected File getFileRelativePath(String rootFolderString, String filePathString) {
    File rootFolder = new File(rootFolderString);
    File filePath = new File(filePathString);

    File fileRelativePath = new File(filePath.getName());

    File parent = filePath.getParentFile();

    while(parent != null && rootFolder.compareTo(parent) < 0) {
      fileRelativePath = new File(parent.getName(), fileRelativePath.getPath());
      parent = parent.getParentFile();
    }

    return fileRelativePath;
  }

}
