package net.dankito.sync.synchronization;


import net.dankito.sync.BaseEntity;
import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookUpKeys;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.SyncState;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.modules.ReadEntitiesCallback;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SyncConfigurationManagerBase implements ISyncConfigurationManager {

  protected static final int SYNC_MODULES_WITH_ENTITY_UPDATES_TIMER_DELAY = 3 * 1000; // 3 seconds delay

  private static final Logger log = LoggerFactory.getLogger(SyncConfigurationManagerBase.class);


  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected IFileStorageService fileStorageService;

  protected IThreadPool threadPool;

  protected LocalConfig localConfig;

  protected Map<String, ISyncModule> availableSyncModules = null;

  protected Map<String, Class<? extends SyncEntity>> entityClassTypes = new ConcurrentHashMap<>();

  protected Set<ISyncModule> syncModulesWithEntityChanges = new HashSet<>();

  protected Timer syncModulesWithEntityUpdatesTimer = new Timer("SyncModulesWithEntityUpdatesTimer");

  protected List<DiscoveredDevice> connectedSynchronizedDevices = new ArrayList<>();


  public SyncConfigurationManagerBase(ISyncManager syncManager, IEntityManager entityManager, IDevicesManager devicesManager, IFileStorageService fileStorageService,
                                      IThreadPool threadPool, LocalConfig localConfig) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.devicesManager = devicesManager;
    this.fileStorageService = fileStorageService;
    this.threadPool = threadPool;
    this.localConfig = localConfig;

    syncManager.addSynchronizationListener(synchronizationListener);
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
      syncModule.readAllEntitiesAsync(new ReadEntitiesCallback() {
        @Override
        public void done(List<SyncEntity> entities) {
          getSyncEntityChangesAndPushToRemote(remoteDevice, syncModuleConfiguration, entities);
        }
      });
    }
  }

  protected void getSyncEntityChangesAndPushToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    List<SyncEntity> entitiesToSync = getEntitiesToSynchronize(remoteDevice, syncModuleConfiguration, entities);
    pushSyncEntitiesToRemote(remoteDevice, syncModuleConfiguration, entitiesToSync);
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

  /**
   * SyncEntity's database id has to be set to be able to find its SyncEntityLocalLookUpKeys
   * @param syncModuleConfiguration
   * @param entity
   * @return
   */
  protected SyncEntityLocalLookUpKeys getLookUpKeyForSyncEntityByDatabaseId(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    String entityId = entity.getId();
    List<SyncEntityLocalLookUpKeys> allLookUpKeys = entityManager.getAllEntitiesOfType(SyncEntityLocalLookUpKeys.class);

    for(SyncEntityLocalLookUpKeys lookUpKey : allLookUpKeys) {
      if(syncModuleConfiguration == lookUpKey.getSyncModuleConfiguration()) {
        if(entityId.equals(lookUpKey.getEntityDatabaseId())) {
          return lookUpKey;
        }
      }
    }

    return null;
  }

  protected SyncEntityState shouldEntityBeSynchronized(SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookUpKeys> lookUpKeys, SyncEntity entity) {
    SyncEntityState type = SyncEntityState.UNCHANGED;

    SyncEntityLocalLookUpKeys entityLookUpKey = lookUpKeys.remove(entity.getLookUpKeyOnSourceDevice()); // remove from lookUpKeys so that in the end only deleted entities remain in  lookUpKeys

    if(entityLookUpKey == null) { // unpersisted SyncEntity
      if(entityManager.persistEntity(entity)) {
        persistEntryLookUpKey(syncModuleConfiguration, entity);
        type = SyncEntityState.CREATED;
      }
    }
    else {
      SyncEntity persistedEntity = entityManager.getEntityById(getEntityClassFromEntityType(entityLookUpKey.getEntityType()), entityLookUpKey.getEntityDatabaseId());
      if(persistedEntity.isDeleted()) { // TODO: how should that ever be? if it's deleted, there's no Entry in Android Database
        deleteEntryLookUpKey(entityLookUpKey);
        type = SyncEntityState.DELETED;
      }
      else {
        if(hasEntityBeenUpdated(entity, entityLookUpKey)) {
          entityLookUpKey.setEntityLastModifiedOnDevice(entity.getLastModifiedOnDevice());
          entityManager.updateEntity(entityLookUpKey);
          type = SyncEntityState.UPDATED;
        }
      }
    }

    return type;
  }

  protected void persistEntryLookUpKey(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncEntityLocalLookUpKeys lookUpKeyEntry = new SyncEntityLocalLookUpKeys(getSyncEntityType(entity), entity.getId(),
                                                                entity.getLookUpKeyOnSourceDevice(), entity.getLastModifiedOnDevice(), syncModuleConfiguration);
    entityManager.persistEntity(lookUpKeyEntry);
  }

  protected void deleteEntryLookUpKey(SyncEntityLocalLookUpKeys lookUpKey) {
    entityManager.deleteEntity(lookUpKey);
  }

  protected boolean hasEntityBeenUpdated(SyncEntity entity, SyncEntityLocalLookUpKeys entityLookupKey) {
    if(entity.getLastModifiedOnDevice() == null) {
      return entityLookupKey.getEntityLastModifiedOnDevice() != null;
    }

    return entity.getLastModifiedOnDevice().equals(entityLookupKey.getEntityLastModifiedOnDevice()) == false;
  }

  protected List<SyncEntity> getDeletedEntities(Map<String, SyncEntityLocalLookUpKeys> lookUpKeys) {
    List<SyncEntity> deletedEntities = new ArrayList<>(lookUpKeys.size());

    for(SyncEntityLocalLookUpKeys lookUpKey : lookUpKeys.values()) {
      SyncEntity deletedEntity = entityManager.getEntityById(getEntityClassFromEntityType(lookUpKey.getEntityType()), lookUpKey.getEntityDatabaseId());

      if(deletedEntity != null) {
        deletedEntities.add(deletedEntity);
      }

      deleteEntryLookUpKey(lookUpKey);
    }

    return deletedEntities;
  }


  protected void pushSyncEntitiesToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    for(SyncEntity syncEntity : entities) {
      pushSyncEntityToRemote(remoteDevice, syncModuleConfiguration, syncEntity);
    }
  }

  protected void pushSyncEntityToRemote(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncJobItem jobItem = new SyncJobItem(syncModuleConfiguration, entity, localConfig.getLocalDevice(), remoteDevice.getDevice());
    entityManager.persistEntity(jobItem);
  }


  protected SyncConfiguration getSyncConfigurationForDevice(Device device) {
    SyncConfiguration syncConfiguration = null;
    Device localDevice = localConfig.getLocalDevice();

    for(SyncConfiguration config : localDevice.getSourceSyncConfigurations()) {
      if(device == config.getDestinationDevice()) {
        syncConfiguration = config;
        break;
      }
    }

    if(syncConfiguration == null) {
      for (SyncConfiguration config : localDevice.getDestinationSyncConfigurations()) {
        if (device == config.getSourceDevice()) {
          syncConfiguration = config;
          break;
        }
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

        // TODO: only add SyncEntityChangeListener if ISyncModule gets activated
        for(ISyncModule syncModule : availableSyncModules.values()) {
          syncModule.addSyncEntityChangeListener(syncEntityChangeListener);
        }
      }
    }

    return new ArrayList<>(availableSyncModules.values());
  }


  protected SyncEntityChangeListener syncEntityChangeListener = new SyncEntityChangeListener() {
    @Override
    public void entityChanged(SyncEntityChange syncEntityChange) {
      pushModuleEntityChangesToRemoteDevicesAfterADelay(syncEntityChange);
    }
  };

  /**
   * Some Modules have a lot of changes in a very short period -> don't react on every change, wait some time till all changes are made
   * @param syncEntityChange
   */
  protected void pushModuleEntityChangesToRemoteDevicesAfterADelay(SyncEntityChange syncEntityChange) {
    final ISyncModule syncModule = syncEntityChange.getSyncModule();
    syncModulesWithEntityChanges.add(syncModule);

    syncModulesWithEntityUpdatesTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if(syncModulesWithEntityChanges.remove(syncModule)) { // if syncModule hasn't removed (and therefore processed) yet
          pushModuleEntityChangesToRemoteDevices(syncModule);
        }
      }
    }, SYNC_MODULES_WITH_ENTITY_UPDATES_TIMER_DELAY);
  }

  protected void pushModuleEntityChangesToRemoteDevices(final ISyncModule syncModule) {
    syncModule.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(List<SyncEntity> entities) {
        String syncModuleName = syncModule.getModuleUniqueKey();

        for(DiscoveredDevice connectedDevice : connectedSynchronizedDevices) {
          SyncConfiguration syncConfiguration = getSyncConfigurationForDevice(connectedDevice.getDevice());
          if(syncConfiguration != null) {
            for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
              if(syncModuleName.equals(syncModuleConfiguration.getSyncModuleClassName())) {
                getSyncEntityChangesAndPushToRemote(connectedDevice, syncModuleConfiguration, entities);
              }
            }
          }
        }
      }
    });
  }


  protected SynchronizationListener synchronizationListener = new SynchronizationListener() {
    @Override
    public void entitySynchronized(BaseEntity entity) {
      if(entity instanceof SyncJobItem) {
        SyncJobItem syncJobItem = (SyncJobItem)entity;
        if(isInitializedSyncJobForUs(syncJobItem)) {
          remoteEntitySynchronized((SyncJobItem) entity);
        }
      }
      else if(entity instanceof SyncConfiguration) {
        SyncConfiguration syncConfiguration = (SyncConfiguration)entity;
        if(syncConfiguration.getDestinationDevice() == localConfig.getLocalDevice()) {
          remoteDeviceStartedSynchronizingWithUs(syncConfiguration);
        }
      }
    }
  };

  protected boolean isInitializedSyncJobForUs(SyncJobItem syncJobItem) {
    return syncJobItem.getDestinationDevice() == localConfig.getLocalDevice() && syncJobItem.getState() == SyncState.INITIALIZED;
  }

  protected void remoteDeviceStartedSynchronizingWithUs(final SyncConfiguration syncConfiguration) {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        devicesManager.remoteDeviceStartedSynchronizingWithUs(syncConfiguration.getSourceDevice());
      }
    }, 3000);
  }

  protected void remoteEntitySynchronized(SyncJobItem jobItem) {
    SyncEntity entity = jobItem.getEntity();
    SyncModuleConfiguration syncModuleConfiguration = jobItem.getSyncModuleConfiguration();
    ISyncModule syncModule = getSyncModuleForClassName(syncModuleConfiguration.getSyncModuleClassName());
    SyncEntityState syncEntityState = getSyncEntityState(syncModuleConfiguration, entity);

    syncModule.synchronizedEntityRetrieved(entity, syncEntityState);
  }

  protected SyncEntityState getSyncEntityState(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncEntityLocalLookUpKeys lookupKey = getLookUpKeyForSyncEntityByDatabaseId(syncModuleConfiguration, entity);

    if(lookupKey == null) {
      persistEntryLookUpKey(syncModuleConfiguration, entity); // TODO: in this way method got side effects
      return SyncEntityState.CREATED;
    }
    else if(entity.isDeleted()) {
      deleteEntryLookUpKey(lookupKey); // TODO: in this way method got side effects
      return SyncEntityState.DELETED;
    }
    else {
      entity.setLookUpKeyOnSourceDevice(lookupKey.getEntityLocalLookUpKey());
      entity.setLastModifiedOnDevice(lookupKey.getEntityLastModifiedOnDevice());
      return SyncEntityState.UPDATED; // TODO: check if entity really has been updated, e.g. by saving last update timestamp on LookupKey row
    }
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
