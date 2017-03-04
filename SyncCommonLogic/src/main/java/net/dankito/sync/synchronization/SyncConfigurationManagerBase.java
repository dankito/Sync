package net.dankito.sync.synchronization;


import net.dankito.sync.BaseEntity;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.EmailSyncEntity;
import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.PhoneNumberSyncEntity;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookupKeys;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.SyncState;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.KnownSynchronizedDevicesListener;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.files.FileSender;
import net.dankito.sync.synchronization.files.FileSyncJobItem;
import net.dankito.sync.synchronization.files.FileSyncServiceDefaultConfig;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.modules.HandleRetrievedSynchronizedEntityCallback;
import net.dankito.sync.synchronization.modules.HandleRetrievedSynchronizedEntityResult;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.modules.ReadEntitiesCallback;
import net.dankito.sync.synchronization.modules.SyncConfigurationChanges;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.IFileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SyncConfigurationManagerBase implements ISyncConfigurationManager {

  protected static final int SYNC_MODULES_WITH_ENTITY_UPDATES_TIMER_DELAY_WITHOUT_ENTITIES_BEING_SYNCHRONIZED = 3 * 1000; // 3 seconds delay when no entities currently are synchronized

  protected static final int SYNC_MODULES_WITH_ENTITY_UPDATES_TIMER_DELAY_WITH_ENTITIES_BEING_SYNCHRONIZED = 30 * 1000; // 30 seconds delay otherwise

  private static final Logger log = LoggerFactory.getLogger(SyncConfigurationManagerBase.class);


  protected ISyncManager syncManager;

  protected IEntityManager entityManager;

  protected IDevicesManager devicesManager;

  protected IDataMerger dataMerger;

  protected IFileStorageService fileStorageService;

  protected IThreadPool threadPool;

  protected LocalConfig localConfig;

  protected FileSender fileSender;

  protected EntitiesSyncQueue syncQueue;

  protected Map<String, ISyncModule> availableSyncModules = null;

  protected Map<String, Class<? extends SyncEntity>> entityClassTypes = new ConcurrentHashMap<>();

  protected List<SyncEntity> syncEntitiesCurrentlyBeingSynchronized = new CopyOnWriteArrayList<>();

  protected Set<ISyncModule> syncModulesWithEntityChanges = new HashSet<>();

  protected Set<ISyncModule> syncModulesCurrentlyReadingAllEntities = new HashSet<>();

  protected Timer syncModulesWithEntityUpdatesTimer = new Timer("SyncModulesWithEntityUpdatesTimer");

  protected List<DiscoveredDevice> connectedSynchronizedDevices = new ArrayList<>();

  protected Map<ISyncModule, List<DiscoveredDevice>> activatedSyncModules = new ConcurrentHashMap<>();


  public SyncConfigurationManagerBase(ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                      IDataMerger dataMerger, IFileStorageService fileStorageService, IThreadPool threadPool) {
    this.syncManager = syncManager;
    this.entityManager = entityManager;
    this.devicesManager = devicesManager;
    this.dataMerger = dataMerger;
    this.fileStorageService = fileStorageService;
    this.threadPool = threadPool;
    this.localConfig = dataManager.getLocalConfig();

    this.fileSender = new FileSender();
    this.syncQueue = new EntitiesSyncQueue(entityManager, fileStorageService, localConfig.getLocalDevice());

    syncManager.addSynchronizationListener(synchronizationListener);
    devicesManager.addKnownSynchronizedDevicesListener(knownSynchronizedDevicesListener);
  }


  protected abstract List<ISyncModule> retrieveAvailableSyncModules();


  protected void connectedToSynchronizedDevice(DiscoveredDevice remoteDevice) {
    connectedSynchronizedDevices.add(remoteDevice);

    SyncConfiguration syncConfiguration = getSyncConfigurationForDevice(remoteDevice.getDevice());
    if(syncConfiguration != null) {
      startContinuousSynchronizationWithDevice(remoteDevice, syncConfiguration);
    }
  }

  protected void disconnectedFromSynchronizedDevice(DiscoveredDevice disconnectedDevice) {
    connectedSynchronizedDevices.remove(disconnectedDevice);

    for(ISyncModule activeSyncModule : activatedSyncModules.keySet()) {
      if(activatedSyncModules.get(activeSyncModule).contains(disconnectedDevice)) {
        removeSyncEntityChangeListener(disconnectedDevice, activeSyncModule);
      }
    }
  }

  protected void startContinuousSynchronizationWithDevice(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration) {
    for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
      if(syncModuleConfiguration.isEnabled()) {
        setSynchronizedDeviceSyncModuleSettings(remoteDevice, syncConfiguration, syncModuleConfiguration);
      }
    }
  }


  @Override
  public void syncConfigurationHasBeenUpdated(SyncConfiguration syncConfiguration, SyncConfigurationChanges changes) {
    DiscoveredDevice remoteDevice = changes.getRemoteDevice();

    for(ISyncModule deactivatedSyncModule : changes.getDeactivatedSyncModules()) {
      removeSyncEntityChangeListener(remoteDevice, deactivatedSyncModule);
    }

    for(SyncModuleConfiguration activatedSyncModuleConfiguration : changes.getActivatedSyncModuleConfigurations()) {
      if(isSyncingForModuleEnabled(syncConfiguration, activatedSyncModuleConfiguration, remoteDevice)) {
        setSynchronizedDeviceSyncModuleSettings(remoteDevice, syncConfiguration, activatedSyncModuleConfiguration);
      }
    }

    for(SyncModuleConfiguration updatedSyncModuleConfiguration : changes.getUpdatedSyncModuleConfigurations()) {
      if(isSyncingForModuleEnabled(syncConfiguration, updatedSyncModuleConfiguration, remoteDevice)) {
        setSynchronizedDeviceSyncModuleSettings(remoteDevice, syncConfiguration, updatedSyncModuleConfiguration);
      }
    }
  }


  protected void setSynchronizedDeviceSyncModuleSettings(DiscoveredDevice remoteDevice, SyncConfiguration syncConfiguration, SyncModuleConfiguration syncModuleConfiguration) {
    ISyncModule syncModule = getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);
    if(syncModule != null) {
      syncModule.configureLocalSynchronizationSettings(remoteDevice, syncModuleConfiguration);

      if(isSyncingForModuleEnabled(syncConfiguration, syncModuleConfiguration, remoteDevice)) {
        startContinuousSynchronizationForModule(remoteDevice, syncModule, syncModuleConfiguration);
      }
    }
  }

  protected boolean isSyncingForModuleEnabled(SyncConfiguration syncConfiguration, SyncModuleConfiguration syncModuleConfiguration, DiscoveredDevice remoteDevice) {
    return syncModuleConfiguration.isBidirectional() == true || remoteDevice.getDevice() == syncConfiguration.getDestinationDevice();
  }

  protected void startContinuousSynchronizationForModule(final DiscoveredDevice remoteDevice, final ISyncModule syncModule, final SyncModuleConfiguration syncModuleConfiguration) {
    addSyncEntityChangeListener(remoteDevice, syncModule);
    syncModulesCurrentlyReadingAllEntities.add(syncModule);

    syncModule.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(List<SyncEntity> entities) {
        syncModulesCurrentlyReadingAllEntities.remove(syncModule);

        determineEntitiesToSynchronize(remoteDevice, syncModuleConfiguration, entities);

        readingAllEntitiesDoneForModule(syncModule);
      }
    });
  }

  protected void readingAllEntitiesDoneForModule(ISyncModule syncModule) {
    syncEntitiesCurrentlyBeingSynchronized.remove(syncModule);

    if(syncModulesWithEntityChanges.remove(syncModule)) { // while reading all entities changes occurred -> now re-read all entities
      pushModuleEntityChangesToRemoteDevices(syncModule);
    }
  }


  protected void determineEntitiesToSynchronize(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> entities) {
    List<SyncEntity> currentlySynchronizedEntities = new ArrayList<>(syncEntitiesCurrentlyBeingSynchronized); // has to be copied here as between getting lookup get and
    // iterating over currently synchronized entities in shouldEntityBeSynchronized(), entity could get removed from syncEntitiesCurrentlyBeingSynchronized
    Map<String, SyncEntityLocalLookupKeys> lookupKeys = getLookupKeysForSyncModuleConfigurationByLocalLookupKey(syncModuleConfiguration);

    for(int i = entities.size() - 1; i >= 0; i--) {
      SyncEntity entity = entities.remove(i);
      determineIfEntityShouldBeSynchronized(entity, remoteDevice, syncModuleConfiguration, currentlySynchronizedEntities, lookupKeys);
    }

    List<SyncEntity> deletedEntities = getDeletedEntities(lookupKeys, currentlySynchronizedEntities); // SyncEntities still remaining in lookupKeys have been deleted
    for(SyncEntity deletedEntity : deletedEntities) {
      syncQueue.addEntityToPushToRemote(deletedEntity, remoteDevice, syncModuleConfiguration);
    }
  }

  protected void determineIfEntityShouldBeSynchronized(SyncEntity entity, DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration, List<SyncEntity> currentlySynchronizedEntities, Map<String, SyncEntityLocalLookupKeys> lookupKeys) {
    SyncEntityLocalLookupKeys entityLookupKey = lookupKeys.remove(entity.getLocalLookupKey()); // remove from lookupKeys so that in the end only deleted entities remain in  lookupKeys
    Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys = getSyncEntityPropertiesLookupKeys(entity, lookupKeys);

    SyncEntityState type = shouldEntityBeSynchronized(entity, entityLookupKey, entityPropertiesLookupKeys, currentlySynchronizedEntities);

    if(type != SyncEntityState.UNCHANGED) {
      log.info("Entity " + entity + " has SyncEntityState of " + type);
      SyncEntity persistedEntity = handleEntityToBeSynchronized(syncModuleConfiguration, entity, entityLookupKey, entityPropertiesLookupKeys, lookupKeys);
      syncQueue.addEntityToPushToRemote(persistedEntity, remoteDevice, syncModuleConfiguration);
    }
  }

  protected Map<String, SyncEntityLocalLookupKeys> getLookupKeysForSyncModuleConfigurationByDatabaseId(SyncModuleConfiguration syncModuleConfiguration) {
    Map<String, SyncEntityLocalLookupKeys> syncModuleConfigurationLookupKeys = new HashMap<>();

    List<SyncEntityLocalLookupKeys> allLookupKeys = entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class);

    for(SyncEntityLocalLookupKeys lookupKey : allLookupKeys) {
      if(syncModuleConfiguration == lookupKey.getSyncModuleConfiguration()) {
        if(lookupKey.getEntityDatabaseId() != null) {
          syncModuleConfigurationLookupKeys.put(lookupKey.getEntityDatabaseId(), lookupKey);
        }
      }
    }

    return syncModuleConfigurationLookupKeys;
  }

  protected Map<String, SyncEntityLocalLookupKeys> getLookupKeysForSyncModuleConfigurationByLocalLookupKey(SyncModuleConfiguration syncModuleConfiguration) {
    Map<String, SyncEntityLocalLookupKeys> syncModuleConfigurationLookupKeys = new HashMap<>();

    List<SyncEntityLocalLookupKeys> allLookupKeys = entityManager.getAllEntitiesOfType(SyncEntityLocalLookupKeys.class);

    for(SyncEntityLocalLookupKeys lookupKey : allLookupKeys) {
      if(syncModuleConfiguration == lookupKey.getSyncModuleConfiguration()) {
        if(lookupKey.getEntityLocalLookupKey() != null) { // otherwise SyncEntity to this lookup key hasn't been added to system storage yet -> no local lookup key
          syncModuleConfigurationLookupKeys.put(lookupKey.getEntityLocalLookupKey(), lookupKey);
        }
      }
    }

    return syncModuleConfigurationLookupKeys;
  }

  protected Map<String, SyncEntityLocalLookupKeys> getSyncEntityPropertiesLookupKeys(SyncEntity entity, Map<String, SyncEntityLocalLookupKeys> lookupKeys) {
    if(entity instanceof ContactSyncEntity) {
      return getContactSyncEntityPropertiesLookupKeys((ContactSyncEntity)entity, lookupKeys);
    }

    return new HashMap<>();
  }

  protected Map<String, SyncEntityLocalLookupKeys> getContactSyncEntityPropertiesLookupKeys(ContactSyncEntity contact, Map<String, SyncEntityLocalLookupKeys> lookupKeys) {
    Map<String, SyncEntityLocalLookupKeys> propertiesLookupKeys = new HashMap<>();

    for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
      SyncEntityLocalLookupKeys lookupKey = lookupKeys.remove(phoneNumber.getLocalLookupKey());
      if(lookupKey != null) {
        propertiesLookupKeys.put(phoneNumber.getLocalLookupKey(), lookupKey);
      }
    }

    for(EmailSyncEntity email : contact.getEmailAddresses()) {
      SyncEntityLocalLookupKeys lookupKey = lookupKeys.remove(email.getLocalLookupKey());
      if(lookupKey != null) {
        propertiesLookupKeys.put(email.getLocalLookupKey(), lookupKey);
      }
    }

    return propertiesLookupKeys;
  }

  protected SyncEntityState shouldEntityBeSynchronized(SyncEntity entity, SyncEntityLocalLookupKeys entityLookupKey, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys, List<SyncEntity> currentlySynchronizedEntities) {
    SyncEntityState type = SyncEntityState.UNCHANGED;

    if(isCurrentlySynchronizedEntity(entity, currentlySynchronizedEntities) == false) {
      if(entityLookupKey == null) { // unpersisted SyncEntity
        type = SyncEntityState.CREATED;
      }
      else {
        SyncEntity persistedEntity = entityManager.getEntityById(getEntityClassFromEntityType(entityLookupKey.getEntityType()), entityLookupKey.getEntityDatabaseId());

        if(persistedEntity.isDeleted()) { // TODO: how should that ever be? if it's deleted, there's no Entry in Android Database
          type = SyncEntityState.DELETED;
        }
        else {
          if(hasEntityBeenUpdated(persistedEntity, entity, entityLookupKey, entityPropertiesLookupKeys)) {
            type = SyncEntityState.UPDATED;
          }
        }
      }
    }

    return type;
  }

  protected boolean isCurrentlySynchronizedEntity(SyncEntity entity, List<SyncEntity> currentlySynchronizedEntities) {
    // check if it's a entity that currently is synchronized that which's local lookup key hasn't been stored to database yet
    for(SyncEntity currentlySynchronizedEntity : currentlySynchronizedEntities) {
      if(entity.getLocalLookupKey().equals(currentlySynchronizedEntity.getLocalLookupKey())) {
        return true;
      }
    }

    return false;
  }

  protected SyncEntity handleEntityToBeSynchronized(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity, SyncEntityLocalLookupKeys entityLookupKey,
                                                    Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(entityLookupKey == null) { // unpersisted SyncEntity
      persistSyncEntityProperties(syncModuleConfiguration, entity);

      persistSyncEntity(syncModuleConfiguration, entity);

      return entity;
    }
    else {
      SyncEntity persistedEntity = entityManager.getEntityById(getEntityClassFromEntityType(entityLookupKey.getEntityType()), entityLookupKey.getEntityDatabaseId());

      if(persistedEntity.isDeleted()) {
        deleteEntityLookupKey(entityLookupKey);
        for(SyncEntityLocalLookupKeys propertyLookupKey : entityPropertiesLookupKeys.values()) {
          deleteEntityLookupKey(propertyLookupKey);
        }
      }
      else {
        entityLookupKey.setEntityLastModifiedOnDevice(entity.getLastModifiedOnDevice());
        entityManager.updateEntity(entityLookupKey);

        updateSyncEntityProperties(syncModuleConfiguration, persistedEntity, entity, entityPropertiesLookupKeys, allLookupKeys);

        mergePersistedEntityWithExtractedOne(persistedEntity, entity);
      }

      return persistedEntity;
    }
  }

  protected void mergePersistedEntityWithExtractedOne(SyncEntity persistedEntity, SyncEntity entity) {
    dataMerger.mergeEntityData(persistedEntity, entity);

    entityManager.updateEntity(persistedEntity);
  }

  protected void persistSyncEntity(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    entity.setSourceDevice(localConfig.getLocalDevice());

    entityManager.persistEntity(entity);

    persistEntryLookupKey(syncModuleConfiguration, entity);
  }

  protected void persistSyncEntityProperties(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    if(entity instanceof ContactSyncEntity) {
      persistContactSyncEntityProperties(syncModuleConfiguration, (ContactSyncEntity)entity);
    }
  }

  protected void persistContactSyncEntityProperties(SyncModuleConfiguration syncModuleConfiguration, ContactSyncEntity contact) {
    for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
      persistSyncEntity(syncModuleConfiguration, phoneNumber);
    }

    for(EmailSyncEntity email : contact.getEmailAddresses()) {
      persistSyncEntity(syncModuleConfiguration, email);
    }
  }

  protected SyncEntityLocalLookupKeys persistEntryLookupKey(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity) {
    SyncEntityLocalLookupKeys lookupKeyEntry = new SyncEntityLocalLookupKeys(getSyncEntityType(entity), entity.getId(),
                                                                entity.getLocalLookupKey(), entity.getLastModifiedOnDevice(), syncModuleConfiguration);
    entityManager.persistEntity(lookupKeyEntry);

    return lookupKeyEntry;
  }

  protected void updateSyncEntityProperties(SyncModuleConfiguration syncModuleConfiguration, SyncEntity persistedEntity, SyncEntity entity,
                                            Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(entity instanceof ContactSyncEntity) {
      updateContactSyncEntityProperties(syncModuleConfiguration, (ContactSyncEntity) persistedEntity, (ContactSyncEntity)entity, entityPropertiesLookupKeys, allLookupKeys);
    }
  }

  protected void updateContactSyncEntityProperties(SyncModuleConfiguration syncModuleConfiguration, ContactSyncEntity persistedContact, ContactSyncEntity contact,
                                                   Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    for(PhoneNumberSyncEntity phoneNumber : new ArrayList<>(persistedContact.getPhoneNumbers())) {
      updatePersistedPhoneNumber(syncModuleConfiguration, persistedContact, phoneNumber, contact, entityPropertiesLookupKeys, allLookupKeys);
    }

    for(PhoneNumberSyncEntity phoneNumber : new ArrayList<>(contact.getPhoneNumbers())) {
      handleUnpersistedPhoneNumber(syncModuleConfiguration, persistedContact, phoneNumber);
    }

    for(EmailSyncEntity email : new ArrayList<>(persistedContact.getEmailAddresses())) {
      updatePersistedEmail(syncModuleConfiguration, persistedContact, email, contact, entityPropertiesLookupKeys, allLookupKeys);
    }

    for(EmailSyncEntity email : new ArrayList<>(contact.getEmailAddresses())) {
      handleUnpersistedEmail(syncModuleConfiguration, persistedContact, email);
    }
  }

  protected void updatePersistedPhoneNumber(SyncModuleConfiguration syncModuleConfiguration, ContactSyncEntity persistedContact, PhoneNumberSyncEntity phoneNumber,
                                            ContactSyncEntity unpersistedContact, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    SyncEntityLocalLookupKeys lookupKey = entityPropertiesLookupKeys.get(phoneNumber.getLocalLookupKey());

    if(lookupKey == null) { // property has been removed
      persistedContact.removePhoneNumber(phoneNumber);
      lookupKey = allLookupKeys.remove(phoneNumber.getLocalLookupKey());

      handleDeletedSyncEntityProperty(syncModuleConfiguration, persistedContact, phoneNumber, lookupKey);
    }
    else {
      PhoneNumberSyncEntity matchingPhoneNumber = getMatchingPhoneNumber(phoneNumber, unpersistedContact);
      if(matchingPhoneNumber != null && hasPhoneNumberBeenUpdated(phoneNumber, matchingPhoneNumber)) {
        dataMerger.mergeEntityData(phoneNumber, matchingPhoneNumber);
        entityManager.updateEntity(phoneNumber);
      }
    }
  }

  protected void updatePersistedEmail(SyncModuleConfiguration syncModuleConfiguration, ContactSyncEntity persistedContact, EmailSyncEntity email,
                                      ContactSyncEntity unpersistedContact, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    SyncEntityLocalLookupKeys lookupKey = entityPropertiesLookupKeys.get(email.getLocalLookupKey());

    if(lookupKey == null) { // property has been removed
      persistedContact.removeEmailAddress(email);
      lookupKey = allLookupKeys.remove(email.getLocalLookupKey());

      handleDeletedSyncEntityProperty(syncModuleConfiguration, persistedContact, email, lookupKey);
    }
    else {
      EmailSyncEntity matchingEmail = getMatchingEmail(email, unpersistedContact);
      if(matchingEmail != null && hasEmailBeenUpdated(email, matchingEmail)) {
        dataMerger.mergeEntityData(email, matchingEmail);
        entityManager.updateEntity(email);
      }
    }
  }

  protected void handleDeletedSyncEntityProperty(SyncModuleConfiguration syncModuleConfiguration, SyncEntity entity, SyncEntity property, SyncEntityLocalLookupKeys lookupKey) {
    entityManager.deleteEntity(property);

    entityManager.deleteEntity(lookupKey);

    ISyncModule syncModule = getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);
    if(syncModule != null) {
      syncModule.deleteSyncEntityProperty(entity, property);
    }
  }

  protected void handleUnpersistedPhoneNumber(SyncModuleConfiguration syncModuleConfiguration, ContactSyncEntity persistedContact, PhoneNumberSyncEntity phoneNumber) {
    boolean isPersisted = isSyncEntityPropertyPersisted(phoneNumber.getLocalLookupKey(), new ArrayList<SyncEntity>(persistedContact.getPhoneNumbers()));

    if(isPersisted == false) {
      entityManager.persistEntity(phoneNumber);
      persistedContact.addPhoneNumber(phoneNumber);
      persistEntryLookupKey(syncModuleConfiguration, phoneNumber);
    }
  }

  protected void handleUnpersistedEmail(SyncModuleConfiguration syncModuleConfiguration, ContactSyncEntity persistedContact, EmailSyncEntity email) {
    boolean isPersisted = isSyncEntityPropertyPersisted(email.getLocalLookupKey(), new ArrayList<SyncEntity>(persistedContact.getEmailAddresses()));

    if(isPersisted == false) {
      entityManager.persistEntity(email);
      persistedContact.addEmailAddress(email);
      persistEntryLookupKey(syncModuleConfiguration, email);
    }
  }

  protected boolean isSyncEntityPropertyPersisted(String unpersistedEntityLookupKey, Collection<SyncEntity> persistedSyncEntityCollection) {
    boolean isPersisted = false;

    for(SyncEntity persistedEntity : persistedSyncEntityCollection) {
      if(unpersistedEntityLookupKey.equals(persistedEntity.getLocalLookupKey())) {
        isPersisted = true;
        break;
      }
    }
    return isPersisted;
  }

  protected void deleteEntityLookupKey(SyncEntityLocalLookupKeys lookupKey) {
    entityManager.deleteEntity(lookupKey);
  }

  protected void deleteSyncEntityPropertiesLookupKey(SyncEntity entity, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(entity instanceof ContactSyncEntity) {
      deleteContactSyncEntityPropertiesLookupKey((ContactSyncEntity)entity, allLookupKeys);
    }
  }

  protected void deleteContactSyncEntityPropertiesLookupKey(ContactSyncEntity contact, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
      SyncEntityLocalLookupKeys lookupKey = allLookupKeys.get(phoneNumber.getId());
      if(lookupKey != null) {
        deleteEntityLookupKey(lookupKey);
      }
    }

    for(EmailSyncEntity email : contact.getEmailAddresses()) {
      SyncEntityLocalLookupKeys lookupKey = allLookupKeys.get(email.getId());
      if(lookupKey != null) {
        deleteEntityLookupKey(lookupKey);
      }
    }
  }

  protected boolean hasEntityBeenUpdated(SyncEntity persistedEntity, SyncEntity entity, SyncEntityLocalLookupKeys entityLookupKey, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys) {
    if(hasEntityBeenUpdated(entity, entityLookupKey)) {
      return true;
    }
    else {
      return hasEntityPropertyBeenUpdated(persistedEntity, entity, entityPropertiesLookupKeys);
    }
  }

  protected boolean hasEntityBeenUpdated(SyncEntity entity, SyncEntityLocalLookupKeys entityLookupKey) {
    if(entity.getLastModifiedOnDevice() == null) {
      return entityLookupKey.getEntityLastModifiedOnDevice() != null;
    }

    return entity.getLastModifiedOnDevice().equals(entityLookupKey.getEntityLastModifiedOnDevice()) == false;
  }

  protected boolean hasEntityPropertyBeenUpdated(SyncEntity persistedEntity, SyncEntity entity, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys) {
    if(entity instanceof ContactSyncEntity) {
      return hasContactPropertyBeenUpdated((ContactSyncEntity)persistedEntity, (ContactSyncEntity)entity, entityPropertiesLookupKeys);
    }

    return false;
  }

  protected boolean hasContactPropertyBeenUpdated(ContactSyncEntity persistedEntity, ContactSyncEntity contact, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys) {
    if(hasPhoneNumberBeenUpdated(persistedEntity, contact, entityPropertiesLookupKeys)) {
      return true;
    }

    if(hasEmailBeenUpdated(persistedEntity, contact, entityPropertiesLookupKeys)) {
      return true;
    }

    return false;
  }

  protected boolean hasPhoneNumberBeenUpdated(ContactSyncEntity persistedEntity, ContactSyncEntity contact, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys) {
    if(persistedEntity.getPhoneNumbers().size() != contact.getPhoneNumbers().size()) {
      return true;
    }

    for(PhoneNumberSyncEntity phoneNumber : persistedEntity.getPhoneNumbers()) {
      PhoneNumberSyncEntity matchingPhoneNumber = getMatchingPhoneNumber(phoneNumber, contact);
      if(matchingPhoneNumber == null) { // this phone number has been deleted
        return true;
      }
      else {
        if(hasPhoneNumberBeenUpdated(phoneNumber, matchingPhoneNumber)) {
          return true;
        }
      }
    }

    return false;
  }

  protected PhoneNumberSyncEntity getMatchingPhoneNumber(PhoneNumberSyncEntity persistedPhoneNumber, ContactSyncEntity unpersistedContact) {
    for(PhoneNumberSyncEntity unpersistedPhoneNumber : unpersistedContact.getPhoneNumbers()) {
      if(persistedPhoneNumber.getLocalLookupKey().equals(unpersistedPhoneNumber.getLocalLookupKey())) {
        return unpersistedPhoneNumber;
      }
    }

    return null;
  }

  protected boolean hasPhoneNumberBeenUpdated(PhoneNumberSyncEntity persistedPhoneNumber, PhoneNumberSyncEntity unpersistedMatchingPhoneNumber) {
    if(persistedPhoneNumber.getNumber() != null && persistedPhoneNumber.getNumber().equals(unpersistedMatchingPhoneNumber.getNumber()) == false) {
      return true;
    }
    else if(persistedPhoneNumber.getNumber() == null && unpersistedMatchingPhoneNumber.getNumber() != null) {
      return true;
    }
    if(persistedPhoneNumber.getType() != null && persistedPhoneNumber.getType().equals(unpersistedMatchingPhoneNumber.getType()) == false) {
      return true;
    }
    else if(persistedPhoneNumber.getType() == null && unpersistedMatchingPhoneNumber.getType() != null) {
      return true;
    }

    if(persistedPhoneNumber.getLabel() != null && persistedPhoneNumber.getLabel().equals(unpersistedMatchingPhoneNumber.getLabel()) == false) {
      return true;
    }
    else if(persistedPhoneNumber.getLabel() == null && unpersistedMatchingPhoneNumber.getLabel() != null) {
      return true;
    }

    return false;
  }

  protected boolean hasEmailBeenUpdated(ContactSyncEntity persistedEntity, ContactSyncEntity contact, Map<String, SyncEntityLocalLookupKeys> entityPropertiesLookupKeys) {
    if(persistedEntity.getEmailAddresses().size() != contact.getEmailAddresses().size()) {
      return true;
    }

    for(EmailSyncEntity email : persistedEntity.getEmailAddresses()) {
      EmailSyncEntity matchingEmail = getMatchingEmail(email, contact);
      if(matchingEmail == null) { // this email has been deleted
        return true;
      }
      else {
        if(hasEmailBeenUpdated(email, matchingEmail)) {
          return true;
        }
      }
    }

    return false;
  }

  protected EmailSyncEntity getMatchingEmail(EmailSyncEntity persistedEmail, ContactSyncEntity unpersistedContact) {
    for(EmailSyncEntity unpersistedEmail : unpersistedContact.getEmailAddresses()) {
      if(persistedEmail.getLocalLookupKey().equals(unpersistedEmail.getLocalLookupKey())) {
        return unpersistedEmail;
      }
    }

    return null;
  }

  protected boolean hasEmailBeenUpdated(EmailSyncEntity persistedEmail, EmailSyncEntity unpersistedMatchingEmail) {
    if(persistedEmail.getAddress() != null && persistedEmail.getAddress().equals(unpersistedMatchingEmail.getAddress()) == false) {
      return true;
    }
    else if(persistedEmail.getAddress() == null && unpersistedMatchingEmail.getAddress() != null) {
      return true;
    }
    if(persistedEmail.getType() != null && persistedEmail.getType().equals(unpersistedMatchingEmail.getType()) == false) {
      return true;
    }
    else if(persistedEmail.getType() == null && unpersistedMatchingEmail.getType() != null) {
      return true;
    }

    if(persistedEmail.getLabel() != null && persistedEmail.getLabel().equals(unpersistedMatchingEmail.getLabel()) == false) {
      return true;
    }
    else if(persistedEmail.getLabel() == null && unpersistedMatchingEmail.getLabel() != null) {
      return true;
    }

    return false;
  }

  protected List<SyncEntity> getDeletedEntities(Map<String, SyncEntityLocalLookupKeys> lookupKeys, List<SyncEntity> currentlySynchronizedEntities) {
    List<SyncEntity> deletedEntities = new ArrayList<>(lookupKeys.size());

    for(SyncEntityLocalLookupKeys lookupKey : lookupKeys.values()) {
      SyncEntity deletedEntity = deleteEntityWithLookupKey(deletedEntities, lookupKey, currentlySynchronizedEntities);

      if(deletedEntity != null) { // TODO: delete entity locally
        deletedEntities.add(deletedEntity);
      }
    }

    return deletedEntities;
  }

  protected SyncEntity deleteEntityWithLookupKey(List<SyncEntity> deletedEntities, SyncEntityLocalLookupKeys lookupKey, List<SyncEntity> currentlySynchronizedEntities) {
    try {
      SyncEntity deletedEntity = entityManager.getEntityById(getEntityClassFromEntityType(lookupKey.getEntityType()), lookupKey.getEntityDatabaseId());
      if(currentlySynchronizedEntities.contains(deletedEntity) == false) {
        if(deletedEntity != null) {
          deletedEntities.add(deletedEntity);
        }

        deleteEntityLookupKey(lookupKey);

        return deletedEntity;
      }
    } catch(Exception e) {
      log.error("Could not delete entity with lookup key " + lookupKey, e);
    }

    return null;
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

  @Override
  public ISyncModule getSyncModuleForSyncModuleConfiguration(SyncModuleConfiguration syncModuleConfiguration) {
    getAvailableSyncModules(); // ensure availableSyncModules are loaded
    return availableSyncModules.get(syncModuleConfiguration.getSyncModuleType());
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
        availableSyncModules = new HashMap<>();

        for(ISyncModule syncModule : retrieveAvailableSyncModules()) {
          availableSyncModules.put(syncModule.getSyncEntityTypeItCanHandle(), syncModule);
        }
      }
    }

    return new ArrayList<>(availableSyncModules.values());
  }


  protected synchronized void addSyncEntityChangeListener(DiscoveredDevice remoteDevice, ISyncModule syncModule) {
    if(activatedSyncModules.containsKey(syncModule)) {
      activatedSyncModules.get(syncModule).add(remoteDevice);
    }
    else {
      List<DiscoveredDevice> devicesThatActivatedThatModule = new ArrayList<>();
      devicesThatActivatedThatModule.add(remoteDevice);

      activatedSyncModules.put(syncModule, devicesThatActivatedThatModule);

      syncModule.addSyncEntityChangeListener(syncEntityChangeListener);
    }
  }

  protected synchronized void removeSyncEntityChangeListener(DiscoveredDevice remoteDevice, ISyncModule syncModule) {
    if(activatedSyncModules.containsKey(syncModule)) {
      List<DiscoveredDevice> devicesThatActivatedThatModule = activatedSyncModules.get(syncModule);
      devicesThatActivatedThatModule.remove(remoteDevice);

      if(devicesThatActivatedThatModule.size() == 0) {
        activatedSyncModules.remove(syncModule);

        syncModule.removeSyncEntityChangeListener(syncEntityChangeListener);
      }
    }
  }

  protected SyncEntityChangeListener syncEntityChangeListener = new SyncEntityChangeListener() {
    @Override
    public void entityChanged(SyncEntityChange syncEntityChange) {
      log.info("SyncEntityChangeListener called for " + syncEntityChange.getSyncModule().getClass().getSimpleName());
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

    if(syncModulesCurrentlyReadingAllEntities.contains(syncModule) == false) { // avoid that while all entities are read readAllEntities is called again for that SyncModule
      int delay = getDelayBeforePushingEntityChangesToRemote();

      if(delay <= 0) {
        syncModulesWithEntityChanges.remove(syncModule);

        pushModuleEntityChangesToRemoteDevices(syncModule);
      }
      else {
        syncModulesWithEntityUpdatesTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            if(syncModulesWithEntityChanges.remove(syncModule)) { // if syncModule hasn't been removed (and therefore processed) yet
              pushModuleEntityChangesToRemoteDevices(syncModule);
            }
          }
        }, delay);
      }
    }
  }

  protected int getDelayBeforePushingEntityChangesToRemote() {
    if(syncEntitiesCurrentlyBeingSynchronized.size() > 0) {
      return SYNC_MODULES_WITH_ENTITY_UPDATES_TIMER_DELAY_WITH_ENTITIES_BEING_SYNCHRONIZED;
    }
    else {
      return SYNC_MODULES_WITH_ENTITY_UPDATES_TIMER_DELAY_WITHOUT_ENTITIES_BEING_SYNCHRONIZED;
    }
  }

  protected void pushModuleEntityChangesToRemoteDevices(final ISyncModule syncModule) {
    syncModulesCurrentlyReadingAllEntities.add(syncModule);

    syncModule.readAllEntitiesAsync(new ReadEntitiesCallback() {
      @Override
      public void done(List<SyncEntity> entities) {
        syncModulesCurrentlyReadingAllEntities.remove(syncModule);

        for(DiscoveredDevice connectedDevice : connectedSynchronizedDevices) {
          SyncConfiguration syncConfiguration = getSyncConfigurationForDevice(connectedDevice.getDevice());
          if(syncConfiguration != null) {
            for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
              if(syncModuleConfiguration.isEnabled()) {
                if(syncModule.getSyncEntityTypeItCanHandle().equals(syncModuleConfiguration.getSyncModuleType())) {
                  determineEntitiesToSynchronize(connectedDevice, syncModuleConfiguration, entities);
                }
              }
            }
          }
        }

        readingAllEntitiesDoneForModule(syncModule);
      }
    });
  }


  protected SynchronizationListener synchronizationListener = new SynchronizationListener() {
    @Override
    public void entitySynchronized(BaseEntity entity) {
      if(entity instanceof SyncJobItem) {
        retrievedSynchronizedSyncJobItem((SyncJobItem) entity);
      }
      else if(entity instanceof SyncConfiguration) {
        SyncConfiguration syncConfiguration = (SyncConfiguration)entity;
        if(syncConfiguration.getDestinationDevice() == localConfig.getLocalDevice() || syncConfiguration.getSourceDevice() == localConfig.getLocalDevice()) {
          syncConfigurationUpdated(syncConfiguration);
        }
      }
    }
  };

  protected void retrievedSynchronizedSyncJobItem(SyncJobItem syncJobItem) {
    if(isInitializedSyncJobWeAreDestinationFor(syncJobItem)) {
      remoteEntitySynchronized(syncJobItem);
    }
    else if(areWeSourceOfSyncJobItem(syncJobItem)) {
      if(isFileRemoteAwaitsThatTransferStarts(syncJobItem)) {
        remoteRetrievedOurFileSyncJobItem(syncJobItem);
      }
    }
  }

  protected boolean isInitializedSyncJobWeAreDestinationFor(SyncJobItem syncJobItem) {
    return syncJobItem.getDestinationDevice() == localConfig.getLocalDevice() && syncJobItem.getState() == SyncState.INITIALIZED;
  }

  protected boolean areWeSourceOfSyncJobItem(SyncJobItem syncJobItem) {
    return syncJobItem.getSourceDevice() == localConfig.getLocalDevice();
  }

  protected boolean isFileRemoteAwaitsThatTransferStarts(SyncJobItem syncJobItem) {
    return syncJobItem.getEntity() instanceof FileSyncEntity && syncJobItem.getState() == SyncState.TRANSFERRING_FILE_TO_DESTINATION_DEVICE;
  }


  protected void syncConfigurationUpdated(SyncConfiguration syncConfiguration) {
    Device remoteDevice = syncConfiguration.getSourceDevice() == localConfig.getLocalDevice() ? syncConfiguration.getDestinationDevice() : syncConfiguration.getSourceDevice();
    DiscoveredDevice discoveredRemoteDevice = getDiscoveredDeviceForDevice(remoteDevice);

    if(discoveredRemoteDevice != null) {
      // TODO: here's a Bug. Sometimes a remote device stops syncing, but remote device is not in connectedSynchronizedDevices
      if(syncConfiguration.getDestinationDevice() == localConfig.getLocalDevice() && connectedSynchronizedDevices.contains(discoveredRemoteDevice) == false) {
        remoteDeviceStartedSynchronizingWithUs(syncConfiguration);
      }
      else if(syncConfiguration.isDeleted()) {
        devicesManager.stopSynchronizingWithDevice(discoveredRemoteDevice);
      }
      else {
        SyncConfigurationChanges changes = getSyncConfigurationChanges(syncConfiguration, discoveredRemoteDevice);
        syncConfigurationHasBeenUpdated(syncConfiguration, changes);
      }
    }
  }

  protected SyncConfigurationChanges getSyncConfigurationChanges(SyncConfiguration syncConfiguration, DiscoveredDevice remoteDevice) {
    SyncConfigurationChanges changes = new SyncConfigurationChanges(remoteDevice);

    for(ISyncModule syncModule : activatedSyncModules.keySet()) {
      boolean remoteDeviceHadThisModuleActive = remoteDeviceHadThisModuleActive(syncModule, remoteDevice);

      AtomicBoolean remoteDeviceHasThisModuleNowActive = new AtomicBoolean(false);
      SyncModuleConfiguration syncModuleConfigurationDeviceHasNowActive =
          remoteDeviceHasThisModuleNowActive(syncConfiguration, syncModule, remoteDeviceHasThisModuleNowActive);

      if(remoteDeviceHadThisModuleActive == false && remoteDeviceHasThisModuleNowActive.get() == true) {
        changes.addActivatedSyncModuleConfiguration(syncModuleConfigurationDeviceHasNowActive);
      }
      else if(remoteDeviceHadThisModuleActive == true) {
        if(remoteDeviceHasThisModuleNowActive.get() == false) {
          changes.addDeactivatedSyncModule(syncModule);
        }
        else {
          changes.addUpdatedSyncModuleConfiguration(syncModuleConfigurationDeviceHasNowActive);
        }
      }
    }

    // check for added SyncModuleConfiguration that aren't activated on this side yet
    for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
      ISyncModule syncModule = getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);
      if(activatedSyncModules.containsKey(syncModule) == false) {
        changes.addActivatedSyncModuleConfiguration(syncModuleConfiguration);
      }
    }

    return changes;
  }

  protected boolean remoteDeviceHadThisModuleActive(ISyncModule syncModule, DiscoveredDevice discoveredRemoteDevice) {
    boolean remoteDeviceHadThisModuleActive = false;

    List<DiscoveredDevice> devicesOnThatModules = activatedSyncModules.get(syncModule);
    if(devicesOnThatModules != null) {
      for(DiscoveredDevice device : devicesOnThatModules) {
        if(device == discoveredRemoteDevice) {
          remoteDeviceHadThisModuleActive = true;
          break;
        }
      }
    }

    return remoteDeviceHadThisModuleActive;
  }

  protected SyncModuleConfiguration remoteDeviceHasThisModuleNowActive(SyncConfiguration syncConfiguration, ISyncModule syncModule, AtomicBoolean remoteDeviceHasThisModuleNowActive) {
    SyncModuleConfiguration syncModuleConfigurationDeviceHasNowActive = null;

    for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
      if(syncModule == getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration)) {
        if(syncModuleConfiguration.isEnabled()) {
          remoteDeviceHasThisModuleNowActive.set(true);
          syncModuleConfigurationDeviceHasNowActive = syncModuleConfiguration;
        }
        break;
      }
    }

    return syncModuleConfigurationDeviceHasNowActive;
  }

  protected void remoteDeviceStartedSynchronizingWithUs(final SyncConfiguration syncConfiguration) {
    configureLocalSyncModuleSettings(syncConfiguration, getDiscoveredDeviceForDevice(syncConfiguration.getSourceDevice()));

    // TODO: what is this timer good for?
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        devicesManager.remoteDeviceStartedSynchronizingWithUs(syncConfiguration.getSourceDevice());
      }
    }, 3000);
  }

  protected void configureLocalSyncModuleSettings(SyncConfiguration syncConfiguration, DiscoveredDevice remoteDevice) {
    for(SyncModuleConfiguration syncModuleConfiguration : syncConfiguration.getSyncModuleConfigurations()) {
      ISyncModule syncModule = getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);
      syncModule.configureLocalSynchronizationSettings(remoteDevice, syncModuleConfiguration);
    }
  }


  protected void remoteEntitySynchronized(final SyncJobItem jobItem) {
    SyncEntity entity = jobItem.getEntity();
    if(entity instanceof FileSyncEntity == false) {
      jobItem.setState(SyncState.TRANSFERRED_TO_DESTINATION_DEVICE);
      entityManager.updateEntity(jobItem);
    }

    final SyncModuleConfiguration syncModuleConfiguration = jobItem.getSyncModuleConfiguration();
    ISyncModule syncModule = getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);

    final SyncEntityState syncEntityState;
    final Map<String, SyncEntityLocalLookupKeys> allLookupKeys = getLookupKeysForSyncModuleConfigurationByDatabaseId(syncModuleConfiguration);
    SyncEntityLocalLookupKeys lookupKey = allLookupKeys.remove(entity.getId());
    if(lookupKey == null) {
      lookupKey = persistEntryLookupKey(syncModuleConfiguration, entity); // persist already here so that we know that we know SyncEntity with this database id
      syncEntityState = SyncEntityState.CREATED;
    }
    else {
      setSyncEntityLocalValuesFromLookupKey(entity, lookupKey);
      syncEntityState = getSyncEntityState(jobItem.getEntity(), lookupKey);
    }

    syncEntitiesCurrentlyBeingSynchronized.add(entity); // when calling handleRetrievedSynchronizedEntityAsync() shortly after syncEntityChangeListener gets called, but its local lookup key hasn't been stored yet to database at this point

    log.info("Retrieved synchronized entity " + jobItem.getEntity() + " of SyncEntityState " + syncEntityState);

    if(syncModule != null) {
      final SyncEntityLocalLookupKeys finalLookupKey = lookupKey;

      syncModule.handleRetrievedSynchronizedEntityAsync(jobItem, syncEntityState, new HandleRetrievedSynchronizedEntityCallback() {
        @Override
        public void done(HandleRetrievedSynchronizedEntityResult result) {
          if(result.isSuccessful()) { // TODO: what to do in error case?
            entitySuccessfullySynchronized(jobItem, finalLookupKey, syncEntityState, syncModuleConfiguration, allLookupKeys);
          }
        }
      });
    }

    syncEntitiesCurrentlyBeingSynchronized.remove(jobItem.getEntity());
  }

  protected void entitySuccessfullySynchronized(SyncJobItem jobItem, SyncEntityLocalLookupKeys lookupKey, SyncEntityState syncEntityState,
                                                SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    handleLookupKeyForSuccessfullySynchronizedEntity(jobItem, lookupKey, syncEntityState, syncModuleConfiguration, allLookupKeys);

    jobItem.setState(SyncState.DONE);
    jobItem.setFinishTime(new Date());

    entityManager.updateEntity(jobItem);

    log.info("Successfully synchronized " + jobItem);
  }

  protected void handleLookupKeyForSuccessfullySynchronizedEntity(SyncJobItem jobItem, SyncEntityLocalLookupKeys lookupKey, SyncEntityState syncEntityState,
                                                                  SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(syncEntityState == SyncEntityState.DELETED) {
      deleteEntityLookupKey(lookupKey);
      deleteSyncEntityPropertiesLookupKey(jobItem.getEntity(), allLookupKeys);
    }
    else {
      updateEntityLookupKeys(jobItem, lookupKey, syncModuleConfiguration, allLookupKeys);
    }
  }

  private void updateEntityLookupKeys(SyncJobItem jobItem, SyncEntityLocalLookupKeys lookupKey, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    SyncEntity entity = jobItem.getEntity();

    updateEntityLookupKey(entity, lookupKey);

    updateSyncEntityPropertiesLookupKeys(entity, syncModuleConfiguration, allLookupKeys);
  }

  protected void updateEntityLookupKey(SyncEntity entity, SyncEntityLocalLookupKeys lookupKey) {
    lookupKey.setEntityLocalLookupKey(entity.getLocalLookupKey());
    lookupKey.setEntityLastModifiedOnDevice(entity.getLastModifiedOnDevice());

    entityManager.updateEntity(lookupKey);
  }

  protected void updateSyncEntityPropertiesLookupKeys(SyncEntity entity, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    if(entity instanceof ContactSyncEntity) {
      updateContactSyncEntityPropertiesLookupKeys((ContactSyncEntity)entity, syncModuleConfiguration, allLookupKeys);
    }
  }

  protected void updateContactSyncEntityPropertiesLookupKeys(ContactSyncEntity contact, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    for(PhoneNumberSyncEntity phoneNumber : contact.getPhoneNumbers()) {
      persistOrUpdateEntityLookupKey(phoneNumber, syncModuleConfiguration, allLookupKeys);
    }

    for(EmailSyncEntity email : contact.getEmailAddresses()) {
      persistOrUpdateEntityLookupKey(email, syncModuleConfiguration, allLookupKeys);
    }
  }

  protected void persistOrUpdateEntityLookupKey(SyncEntity entity, SyncModuleConfiguration syncModuleConfiguration, Map<String, SyncEntityLocalLookupKeys> allLookupKeys) {
    SyncEntityLocalLookupKeys lookupKey = allLookupKeys.get(entity.getId());

    if(lookupKey == null) {
      persistEntryLookupKey(syncModuleConfiguration, entity);
    }
    else {
      updateEntityLookupKey(entity, lookupKey);
    }
  }

  protected SyncEntityState getSyncEntityState(SyncEntity entity, SyncEntityLocalLookupKeys lookupKey) {
    if(lookupKey == null || lookupKey.getEntityLocalLookupKey() == null) {
      return SyncEntityState.CREATED;
    }
    else {
      if(entity.isDeleted()) {
        return SyncEntityState.DELETED;
      }
      else {
        return SyncEntityState.UPDATED; // TODO: check if entity really has been updated, e.g. by saving last update timestamp on LookupKey row
      }
    }
  }

  protected void setSyncEntityLocalValuesFromLookupKey(SyncEntity entity, SyncEntityLocalLookupKeys lookupKey) {
    if(lookupKey != null) {
      entity.setLocalLookupKey(lookupKey.getEntityLocalLookupKey());
      entity.setLastModifiedOnDevice(lookupKey.getEntityLastModifiedOnDevice());
    }
  }


  protected void remoteRetrievedOurFileSyncJobItem(SyncJobItem syncJobItem) {
    FileSyncEntity fileSyncEntity = (FileSyncEntity)syncJobItem.getEntity();
    DiscoveredDevice remoteDevice = getDiscoveredDeviceForDevice(syncJobItem.getDestinationDevice());

    if(remoteDevice != null) {
      FileSyncJobItem fileSyncJobItem = new FileSyncJobItem(syncJobItem, fileSyncEntity.getFilePath(), remoteDevice.getAddress(),
          FileSyncServiceDefaultConfig.FILE_SYNC_SERVICE_DEFAULT_LISTENER_PORT); // TODO: get actual remote's file service port

      fileSender.sendFileAsync(fileSyncJobItem);
    }
  }


  protected DiscoveredDevice getDiscoveredDeviceForDevice(Device device) {
    return devicesManager.getDiscoveredDeviceForDevice(device);
  }


  protected KnownSynchronizedDevicesListener knownSynchronizedDevicesListener = new KnownSynchronizedDevicesListener() {
    @Override
    public void knownSynchronizedDeviceConnected(DiscoveredDevice connectedDevice) {
      connectedToSynchronizedDevice(connectedDevice);
    }

    @Override
    public void knownSynchronizedDeviceDisconnected(DiscoveredDevice disconnectedDevice) {
      disconnectedFromSynchronizedDevice(disconnectedDevice);
    }
  };

}
