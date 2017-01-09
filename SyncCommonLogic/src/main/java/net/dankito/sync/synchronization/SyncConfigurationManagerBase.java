package net.dankito.sync.synchronization;


import net.dankito.sync.Device;
import net.dankito.sync.ISyncModule;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.ReadEntitiesCallback;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookUpKeys;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SyncConfigurationManagerBase implements ISyncConfigurationManager {

  private static final Logger log = LoggerFactory.getLogger(SyncConfigurationManagerBase.class);


  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected LocalConfig localConfig;

  protected Map<String, ISyncModule> availableSyncModules = null;

  protected Map<String, Class<? extends SyncEntity>> entityClassTypes = new ConcurrentHashMap<>();

  protected List<DiscoveredDevice> connectedSynchronizedDevices = new ArrayList<>();


  public SyncConfigurationManagerBase(ISyncManager syncManager, IEntityManager entityManager, IDevicesManager devicesManager, LocalConfig localConfig) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.localConfig = localConfig;

    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);
  }


  protected abstract Map<String, ISyncModule> retrieveAvailableSyncModules();


  protected void connectedToSynchronizedDevice(DiscoveredDevice remoteDevice) {
    connectedSynchronizedDevices.add(remoteDevice);

    SyncConfiguration syncConfiguration = getSyncConfigurationForDevice(remoteDevice.getDevice());
    if(syncConfiguration != null) {
      startContinuouslySynchronizationWithDevice(remoteDevice, syncConfiguration);
    }
  }

  protected void startContinuouslySynchronizationWithDevice(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration) {
    for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
      startContinuouslySynchronizationForModule(remoteDevice, syncModuleConfiguration);
    }
  }

  protected void startContinuouslySynchronizationForModule(final DiscoveredDevice remoteDevice, final SyncModuleConfiguration syncModuleConfiguration) {
    ISyncModule syncModule = getSyncModuleForClassName(syncModuleConfiguration.getSyncModuleClassName());
    if(syncModule != null) {
      syncModule.readAllEntitiesAsync(syncModuleConfiguration, new ReadEntitiesCallback() {
        @Override
        public void done(List<SyncEntity> entities) {
          List<SyncEntity> entitiesToSync = getEntitiesToSynchronize(remoteDevice, syncModuleConfiguration, entities);
          pushSyncEntitiesToRemote(remoteDevice, syncModuleConfiguration, entitiesToSync);
        }
      });
    }
  }


  protected List<SyncEntity> getEntitiesToSynchronize(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    List<SyncEntity> entitiesToSync = new ArrayList<>();

    Map<String, SyncEntityLocalLookUpKeys> lookUpKeys = getLookUpKeysForSyncModuleConfiguration(syncModuleConfiguration);

    for(SyncEntity entity : entities) {
      SyncEntityState type = shouldEntityBeSynchronized(syncModuleConfiguration, lookUpKeys, entity);

      if(type != SyncEntityState.UNCHANGED) {
        entitiesToSync.add(entity);
      }
    }

    List<SyncEntity> deletedEntities = getDeletedEntities(lookUpKeys); // SyncEntities still remaining in lookUpKeys have been deleted
    entitiesToSync.addAll(deletedEntities);

    return entitiesToSync;
  }

  protected Map<String, SyncEntityLocalLookUpKeys> getLookUpKeysForSyncModuleConfiguration(SyncModuleConfiguration syncModuleConfiguration) {
    Map<String, SyncEntityLocalLookUpKeys> syncModuleConfigurationLookUpKeys = new HashMap<>();

    List<SyncEntityLocalLookUpKeys> allLookUpKeys = entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class);

    for(SyncEntityLocalLookUpKeys lookUpKey : allLookUpKeys) {
      if(syncModuleConfiguration == lookUpKey.getSyncModuleConfiguration()) {
        syncModuleConfigurationLookUpKeys.put(lookUpKey.getEntityLocalLookUpKey(), lookUpKey);
      }
    }

    return syncModuleConfigurationLookUpKeys;
  }

  protected SyncEntityState shouldEntityBeSynchronized(SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookUpKeys> lookUpKeys, SyncEntity entity) {
    SyncEntityState type = SyncEntityState.UNCHANGED;

    if(entity.getId() == null) { // unpersisted SyncEntity
      if(entityManager.persistEntity(entity)) {
        persistLookUpKeyEntry(syncModuleConfiguration, entity);
        type = SyncEntityState.CREATED;
      }
    }
    else {
      SyncEntityLocalLookUpKeys entityLookUpKey = lookUpKeys.remove(entity.getLookUpKeyOnSourceDevice()); // remove from lookUpKeys so that in the end only deleted entities remain in  lookUpKeys

      if(entityLookUpKey == null) {
        persistLookUpKeyEntry(syncModuleConfiguration, entity);
        type = SyncEntityState.CREATED;
      }
      else {
        SyncEntity persistedEntity = entityManager.getEntityById(getEntityClassFromEntityType(entityLookUpKey.getEntityType()), entityLookUpKey.getEntityDatabaseId());
        if(persistedEntity.isDeleted()) { // TODO: how should that ever be? if it's deleted, there's no Entry in Android Database
          entityManager.deleteEntity(entityLookUpKey);
          type = SyncEntityState.DELETED;
        }
        else {
          if(hasEntityBeenUpdated(persistedEntity, entity)) {
            type = SyncEntityState.UPDATED;
          }
        }
      }
    }

    return type;
  }

  protected void persistLookUpKeyEntry(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncEntityLocalLookUpKeys lookUpKeyEntry = new SyncEntityLocalLookUpKeys(getSyncEntityType(entity), entity.getId(),
                                                                entity.getLookUpKeyOnSourceDevice(), syncModuleConfiguration);
    entityManager.persistEntity(lookUpKeyEntry);
  }

  protected boolean hasEntityBeenUpdated(SyncEntity persistedEntity, SyncEntity entity) {
    if(persistedEntity.getLastModifiedOnDevice() == null) {
      return entity.getLastModifiedOnDevice() != null;
    }

    return persistedEntity.getLastModifiedOnDevice().equals(entity.getLastModifiedOnDevice()) == false;
  }

  protected List<SyncEntity> getDeletedEntities(Map<String, SyncEntityLocalLookUpKeys> lookUpKeys) {
    List<SyncEntity> deletedEntities = new ArrayList<>(lookUpKeys.size());

    for(SyncEntityLocalLookUpKeys lookUpKey : lookUpKeys.values()) {
      SyncEntity deletedEntity = entityManager.getEntityById(getEntityClassFromEntityType(lookUpKey.getEntityType()), lookUpKey.getEntityDatabaseId());

      if(deletedEntity != null) {
        deletedEntities.add(deletedEntity);
      }

      entityManager.deleteEntity(lookUpKey);
    }

    return deletedEntities;
  }


  protected void pushSyncEntitiesToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    for(SyncEntity syncEntity : entities) {
      pushSyncEntityToRemote(remoteDevice, syncModuleConfiguration, syncEntity);
    }
  }

  protected void pushSyncEntityToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncJobItem jobItem = new SyncJobItem(syncModuleConfiguration, entity);
    entityManager.persistEntity(jobItem);
  }


  protected SyncConfiguration getSyncConfigurationForDevice(Device device) {
    SyncConfiguration syncConfiguration = null;
    Device localDevice = localConfig.getLocalDevice();

    for(SyncConfiguration config : device.getDestinationSyncConfigurations()) {
      if(localDevice == config.getSourceDevice()) {
        syncConfiguration = config;
        break;
      }
    }

    return syncConfiguration;
  }

  protected ISyncModule getSyncModuleForClassName(String syncModuleClassName) {
    getAvailableSyncModules(); // ensure availableSyncModules are loaded
    return availableSyncModules.get(syncModuleClassName);
  }

  protected String getSyncEntityType(SyncEntity entity) {
    return entity.getClass().getName();
  }

  protected Class<? extends SyncEntity> getEntityClassFromEntityType(String entityType) {
    Class<? extends SyncEntity> entityClass = entityClassTypes.get(entityType);

    if(entityClass == null) {
      try {
        entityClass = (Class<? extends SyncEntity>)Class.forName(entityType);

        entityClassTypes.put(entityType, entityClass);
      } catch(Exception e) {
        log.error("Could not get SyncEntity's Class from entityType " + entityType, e);
      }
    }

    return entityClass;
  }


  public List<ISyncModule> getAvailableSyncModules() {
    synchronized(this) {
      if(availableSyncModules == null) {
        availableSyncModules = retrieveAvailableSyncModules();
      }
    }

    return new ArrayList<>(availableSyncModules.values());
  }


  protected DiscoveredDevicesListener discoveredDevicesListener = new DiscoveredDevicesListener() {
    @Override
    public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
      if(type == DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE) {
        connectedToSynchronizedDevice(connectedDevice);
      }
    }

    @Override
    public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {

    }
  };

}
