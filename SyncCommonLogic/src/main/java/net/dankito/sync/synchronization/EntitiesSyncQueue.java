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

  protected AsyncProducerConsumerQueue<EntitiesSyncQueueItem> producerConsumerQueue;


  public EntitiesSyncQueue(IEntityManager entityManager, IFileStorageService fileStorageService, Device localDevice) {
    this.entityManager = entityManager;
    this.fileStorageService = fileStorageService;
    this.localDevice = localDevice;

    this.producerConsumerQueue = new AsyncProducerConsumerQueue<>(1, consumerListener);
  }

  public void addEntityToPushToRemote(SyncEntity entityToPush, DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    producerConsumerQueue.add(new EntitiesSyncQueueItem(entityToPush, remoteDevice, syncModuleConfiguration));
  }


  protected ConsumerListener<EntitiesSyncQueueItem> consumerListener = new ConsumerListener<EntitiesSyncQueueItem>() {
    @Override
    public void consumeItem(EntitiesSyncQueueItem item) {
      pushSyncEntityToRemote(item.getEntityToPush(), item.getRemoteDevice(), item.getSyncModuleConfiguration());
    }
  };

  protected void pushSyncEntityToRemote(SyncEntity entity, DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    log.info("Pushing " + entity + " to remote " + remoteDevice.getDevice() + " ...");

    SyncJobItem jobItem = new SyncJobItem(syncModuleConfiguration, entity, localDevice, remoteDevice.getDevice());
    boolean shouldWaitSomeTimeForSynchronization = false;

    if(entity instanceof FileSyncEntity) {
      System.gc(); // Couchbase Lite causes too much memory consumption with attachments (all data is loaded into memory and then Base64 encoded, which consumes 33 % more space then the original

      String filePath = ((FileSyncEntity)entity).getFilePath(); // TODO: this is not valid on destination device -> use path from LocalLookupKey

      try {
        jobItem.setSyncEntityData(fileStorageService.readFromBinaryFile(filePath));
        log.info("Added file of length " + jobItem.getSyncEntityData().length);
        shouldWaitSomeTimeForSynchronization = true;
      } catch(Exception e) { log.error("Could not read file for FileSyncItem " + entity, e); }
    }

    entityManager.persistEntity(jobItem);

    jobItem.setSyncEntityData(null);

    if(shouldWaitSomeTimeForSynchronization) {
      try { Thread.sleep(30000); } catch(Exception ignored) { }
    }
  }

}
