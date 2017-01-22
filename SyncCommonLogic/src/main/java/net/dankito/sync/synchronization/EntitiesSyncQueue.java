package net.dankito.sync.synchronization;


import net.dankito.sync.Device;
import net.dankito.sync.FileSyncEntity;
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

    this.defaultSyncJobItemsQueue = new AsyncProducerConsumerQueue<>(1, defaultSyncJobItemsConsumerListener);
    this.largerSyncJobItemsQueue = new AsyncProducerConsumerQueue<>(1, largerSyncJobItemsConsumerListener);
  }

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
    DiscoveredDevice remoteDevice = syncQueueItem.getRemoteDevice();
    log.info("Pushing " + entity + " to remote " + remoteDevice.getDevice() + " ...");

    SyncJobItem jobItem = new SyncJobItem(syncQueueItem.getSyncModuleConfiguration(), entity, localDevice, remoteDevice.getDevice());

    if(entity instanceof FileSyncEntity) {
      try {
        jobItem.setDataSize(new File(((FileSyncEntity) entity).getFilePath()).length());
      } catch(Exception e) { log.error("Could not set dataSize on FileSyncEntity " + entity); }
    }

    entityManager.persistEntity(jobItem);

    return jobItem;
  }

  protected void pushLargerSyncEntityToRemote(EntitiesSyncQueueItem syncQueueItem) {
    SyncJobItem jobItem = pushSyncEntityToRemote(syncQueueItem);

    waitSomeTimeBeforePushingNextLargeJobToQueue(jobItem);
  }

  protected void waitSomeTimeBeforePushingNextLargeJobToQueue(SyncJobItem jobItem) {
    try { Thread.sleep(jobItem.getDataSize() / WAIT_TIME_DIVISOR_BEFORE_PUSH_NEXT_FILE); } catch (Exception ignored) { }
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
      pushLargerSyncEntityToRemote(item);
    }
  };

}
