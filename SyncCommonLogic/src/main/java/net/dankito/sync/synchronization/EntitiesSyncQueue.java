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

public class EntitiesSyncQueue {

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


  protected void pushSyncEntityToRemote(EntitiesSyncQueueItem syncQueueItem) {
    pushSyncEntityToRemote(syncQueueItem, null);
  }

  protected void pushSyncEntityToRemote(EntitiesSyncQueueItem syncQueueItem, byte[] syncEntityData) {
    SyncEntity entity = syncQueueItem.getEntityToPush();
    DiscoveredDevice remoteDevice = syncQueueItem.getRemoteDevice();
    log.info("Pushing " + entity + " to remote " + remoteDevice.getDevice() + " ...");

    SyncJobItem jobItem = new SyncJobItem(syncQueueItem.getSyncModuleConfiguration(), entity, localDevice, remoteDevice.getDevice());
    jobItem.setSyncEntityData(syncEntityData);

    entityManager.persistEntity(jobItem);

    jobItem.setSyncEntityData(null);
  }

  protected void pushLargerSyncEntityToRemote(EntitiesSyncQueueItem syncQueueItem) {
    SyncEntity entity = syncQueueItem.getEntityToPush();
    byte[] syncEntityData = null;

    if(entity instanceof FileSyncEntity) {
      String filePath = ((FileSyncEntity)entity).getFilePath(); // TODO: this is not valid on destination device -> use path from LocalLookupKey

      try {
        syncEntityData = fileStorageService.readFromBinaryFile(filePath);
        log.info("Added file of length " + (syncEntityData != null ? syncEntityData.length : 0));
      } catch(Exception e) { log.error("Could not read file for FileSyncItem " + entity, e); }
    }

    pushSyncEntityToRemote(syncQueueItem, syncEntityData);

    if(syncEntityData != null) {
      freeMemory();
    }

    waitSomeTimeBeforePushingNextLargeJobToQueue(syncEntityData);
  }

  /**
   * Attachments are consuming a massive amount of memory in Couchbase Lite as they are first loaded into memory and
   * get Base64 encoded -> it uses at least 233 % of attachment's size in memory.
   * And i'm not sure about that but it seems for pushing it to remote it loads the whole Base64 encoded string again to memory.
   */
  protected void freeMemory() {
    System.gc();
  }

  protected void waitSomeTimeBeforePushingNextLargeJobToQueue(byte[] syncEntityData) {
    // TODO: make wait time configurable and dependent on syncEntityData size and device's memory size
    if(syncEntityData != null) {
      try { Thread.sleep(30000); } catch (Exception ignored) { }
    }
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
