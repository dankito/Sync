package net.dankito.sync.synchronization;


import net.dankito.sync.Device;
import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.OsType;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.utils.AsyncProducerConsumerQueue;
import net.dankito.utils.ConsumerListener;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class EntitiesSyncQueue {

  protected static final int WAIT_TIME_DIVISOR_BEFORE_PUSH_NEXT_FILE = 1024 * 1024;

  private static final Logger log = LoggerFactory.getLogger(EntitiesSyncQueue.class);


  protected IEntityManager entityManager;

  protected IFileStorageService fileStorageService;

  protected Device localDevice;

  protected AsyncProducerConsumerQueue<EntitiesSyncQueueItem> defaultSyncJobItemsQueue;

  protected AsyncProducerConsumerQueue<EntitiesSyncQueueItem> largerSyncJobItemsQueue;


  public EntitiesSyncQueue(IEntityManager entityManager, IFileStorageService fileStorageService, Device localDevice) {
    this.entityManager = entityManager;
    this.fileStorageService = fileStorageService;
    this.localDevice = localDevice;

    this.defaultSyncJobItemsQueue = new AsyncProducerConsumerQueue<>(3, defaultSyncJobItemsConsumerListener);
    this.largerSyncJobItemsQueue = new AsyncProducerConsumerQueue<>(1, largerSyncJobItemsConsumerListener);
  }

  int countPushedSyncJobItems = 0;

  public void addEntityToPushToRemote(SyncEntity entityToPush, DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    EntitiesSyncQueueItem queueItem = new EntitiesSyncQueueItem(entityToPush, remoteDevice, syncModuleConfiguration);

    if(entityToPush instanceof FileSyncEntity) {
      largerSyncJobItemsQueue.add(queueItem);
    }
    else {
      defaultSyncJobItemsQueue.add(queueItem);
    }
  }


  protected SyncJobItem pushSyncEntityToRemote(EntitiesSyncQueueItem syncQueueItem) {
    SyncEntity entity = syncQueueItem.getEntityToPush();
    Device remoteDevice = syncQueueItem.getRemoteDevice().getDevice();
    log.info("[" + countPushedSyncJobItems++ + "] Pushing " + entity + " to remote " + remoteDevice + " ...");

    // TODO: this is duplicated code + another Sonderbehandlung for Thunderbird -> make generic
    Device sourceDevice = remoteDevice.getOsType() == OsType.THUNDERBIRD ? remoteDevice : localDevice;
    Device destinationDevice = remoteDevice.getOsType() == OsType.THUNDERBIRD ? localDevice : remoteDevice;

    SyncJobItem jobItem = new SyncJobItem(syncQueueItem.getSyncModuleConfiguration(), entity, sourceDevice, destinationDevice);

    if(entity instanceof FileSyncEntity) {
      try {
        jobItem.setDataSize(new File(((FileSyncEntity) entity).getFilePath()).length());
      } catch(Exception e) { log.error("Could not set dataSize on FileSyncEntity " + entity); }
    }

    entityManager.persistEntity(jobItem);

    return jobItem;
  }


  protected ConsumerListener<EntitiesSyncQueueItem> defaultSyncJobItemsConsumerListener = new ConsumerListener<EntitiesSyncQueueItem>() {
    @Override
    public void consumeItem(EntitiesSyncQueueItem item) {
      pushSyncEntityToRemote(item);
    }
  };

  protected ConsumerListener<EntitiesSyncQueueItem> largerSyncJobItemsConsumerListener = new ConsumerListener<EntitiesSyncQueueItem>() {
    @Override
    public void consumeItem(EntitiesSyncQueueItem item) {
      pushSyncEntityToRemote(item);
    }
  };

}
