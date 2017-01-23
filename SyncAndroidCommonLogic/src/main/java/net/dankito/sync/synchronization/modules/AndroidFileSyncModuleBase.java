package net.dankito.sync.synchronization.modules;

import android.Manifest;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.PermissionRequestCallback;
import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.files.FileSyncListener;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.sync.synchronization.files.RetrievedFile;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.StringUtils;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AndroidFileSyncModuleBase extends AndroidSyncModuleBase implements ISyncModule, IFileSyncModule {

  private static final Logger log = LoggerFactory.getLogger(AndroidFileSyncModuleBase.class);


  protected FileSyncService fileSyncService;

  protected FileHandler fileHandler;

  protected Map<SyncJobItem, HandleRetrievedSynchronizedEntityCallback> pendingCallbacks = new ConcurrentHashMap<>();


  public AndroidFileSyncModuleBase(Context context, Localization localization, IPermissionsManager permissionsManager, IThreadPool threadPool, FileSyncService fileSyncService, IFileStorageService fileStorageService) {
    super(context, localization, permissionsManager, threadPool);

    this.fileSyncService = fileSyncService;
    fileSyncService.addFileSyncListener(fileSyncListener);
    this.fileHandler = new FileHandler(fileStorageService);
  }


  @Override
  public String getPermissionToReadEntities() {
    return Manifest.permission.READ_EXTERNAL_STORAGE;
  }

  @Override
  public String getPermissionToWriteEntities() {
    return Manifest.permission.WRITE_EXTERNAL_STORAGE;
  }


  @Override
  public void configureLocalSynchronizationSettings(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    super.configureLocalSynchronizationSettings(remoteDevice, syncModuleConfiguration);

    if(StringUtils.isNullOrEmpty(syncModuleConfiguration.getDestinationPath())) {
      File destinationFolder = new File(getRootFolderForStoreRemoteDeviceData(), remoteDevice.getDevice().getDeviceDisplayName());

      syncModuleConfiguration.setDestinationPath(destinationFolder.getAbsolutePath());
    }

    permissionsManager.checkPermission(getPermissionToWriteEntities(), getPermissionRationaleResourceId(), new PermissionRequestCallback() {
      @Override
      public void permissionCheckDone(String permission, boolean isGranted) {

      }
    });
  }

  protected File getRootFolderForStoreRemoteDeviceData() {
    return Environment.getExternalStorageDirectory();
  }


  @Override
  protected boolean addEntityToLocalDatabase(SyncJobItem jobItem) { // in AndroidFileSyncModuleBase they aren't used anymore, it overwrites synchronizedEntityRetrievedPermissionGranted()
    return false;
  }

  @Override
  protected boolean updateEntityInLocalDatabase(SyncJobItem jobItem) { // in AndroidFileSyncModuleBase they aren't used anymore, it overwrites synchronizedEntityRetrievedPermissionGranted()
    return false;
  }

  @Override
  protected void synchronizedEntityRetrievedPermissionGranted(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    if(entityState == SyncEntityState.CREATED || entityState == SyncEntityState.UPDATED) {
      createOrUpdateFile(jobItem, callback);
    }
    else if(entityState == SyncEntityState.DELETED) {
      boolean isSuccessful = fileHandler.deleteFile(jobItem);
      callback.done(new HandleRetrievedSynchronizedEntityResult(jobItem, isSuccessful));
    }
  }


  protected void createOrUpdateFile(SyncJobItem jobItem, HandleRetrievedSynchronizedEntityCallback callback) {
    pendingCallbacks.put(jobItem, callback);

    fileSyncService.start();
    fileSyncService.fileSyncJobItemRetrieved(jobItem);
  }


  protected FileSyncListener fileSyncListener = new FileSyncListener() {
    @Override
    public void fileRetrieved(RetrievedFile retrievedFile) {
      retrievedFile(retrievedFile);
    }
  };

  protected void retrievedFile(RetrievedFile retrievedFile) {
    notifyAndroidSystemOfChangedOrAddedFileAsync((FileSyncEntity)retrievedFile.getJobItem().getEntity(), retrievedFile.getDestinationFile());

    HandleRetrievedSynchronizedEntityCallback callback = pendingCallbacks.remove(retrievedFile.getJobItem());
    if(callback != null) {
      callback.done(new HandleRetrievedSynchronizedEntityResult(retrievedFile.getJobItem(), true));
    }
  }


  @Override
  protected void updateLastModifiedDate(SyncJobItem jobItem) {
    try {
      FileSyncEntity syncEntity = (FileSyncEntity) jobItem.getEntity();

      File file = new File(syncEntity.getFilePath());

      syncEntity.setLastModifiedOnDevice(new Date(file.lastModified()));
    } catch (Exception e) { }
  }


  protected void notifyAndroidSystemOfChangedOrAddedFileAsync(final FileSyncEntity entity, final File file) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        notifyAndroidSystemOfChangedOrAddedFile(entity, file);
      }
    });
  }

  protected void notifyAndroidSystemOfChangedOrAddedFile(FileSyncEntity entity, File file) {
    try {
      MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath()}, new String[] { entity.getMimeType() }, null);
    } catch(Exception e) { log.error("Could not start MediaScanner for inserted image file " + file.getAbsolutePath(), e); }
  }

}
