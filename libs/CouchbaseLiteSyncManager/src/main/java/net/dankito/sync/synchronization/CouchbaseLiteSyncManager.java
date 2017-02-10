package net.dankito.sync.synchronization;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import net.dankito.jpa.annotationreader.config.PropertyConfig;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.sync.BaseEntity;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncEntityLocalLookupKeys;
import net.dankito.sync.config.DatabaseTableConfig;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.utils.AsyncProducerConsumerQueue;
import net.dankito.utils.ConsumerListener;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;


@Named
public class CouchbaseLiteSyncManager extends SyncManagerBase {

  protected static final String FILTER_NAME = "ENTITIES_FILTER";

  protected static final int MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY = 1500;


  private static final Logger log = LoggerFactory.getLogger(CouchbaseLiteSyncManager.class);


  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;

  protected Manager manager;

  protected INetworkSettings networkSettings;

  protected SynchronizedDataMerger dataMerger;

  protected ConflictHandler conflictHandler;

  protected boolean alsoUsePullReplication;

  protected int synchronizationPort;
  protected Credentials allowedCredentials;

  protected Thread listenerThread;

  protected LiteListener couchbaseLiteListener;

  protected Map<String, Replication> pushReplications = new ConcurrentHashMap<>();
  protected Map<String, Replication> pullReplications = new ConcurrentHashMap<>();

  protected AsyncProducerConsumerQueue<Database.ChangeEvent> changeQueue;


  @Inject
  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, INetworkSettings networkSettings, IDevicesManager devicesManager, IThreadPool threadPool) {
    this(entityManager, networkSettings, devicesManager, threadPool, SynchronizationConfig.DEFAULT_SYNCHRONIZATION_PORT, SynchronizationConfig.DEFAULT_ALSO_USE_PULL_REPLICATION);
  }

  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, INetworkSettings networkSettings, IDevicesManager devicesManager,
                                  IThreadPool threadPool, int synchronizationPort, boolean alsoUsePullReplication) {
    super(devicesManager, threadPool);
    this.entityManager = entityManager;
    this.database = entityManager.getDatabase();
    this.manager = database.getManager();
    this.networkSettings = networkSettings;
    this.synchronizationPort = synchronizationPort;
    this.alsoUsePullReplication = alsoUsePullReplication;

    this.dataMerger = new SynchronizedDataMerger(this, entityManager, database);
    this.conflictHandler = new ConflictHandler(entityManager, database);

    // wait some time before processing synchronized entities as they may have dependent entities which haven't been synchronized yet
    this.changeQueue = new AsyncProducerConsumerQueue<>(1, MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY, synchronizationChangesHandler);

    setReplicationFilter(database);
  }

  private void setReplicationFilter(Database database) {
    final List<String> entitiesToFilter = new ArrayList<>();
    entitiesToFilter.add(LocalConfig.class.getName());
    entitiesToFilter.add(SyncEntityLocalLookupKeys.class.getName());

    database.setFilter(FILTER_NAME, new ReplicationFilter() {
      @Override
      public boolean filter(SavedRevision revision, Map<String, Object> params) {
        String entityType = (String)revision.getProperty(Dao.TYPE_COLUMN_NAME);
        return ! ( entitiesToFilter.contains(entityType) );
      }
    });
  }


  public void stop() {
    stopCBLListener();

    stopAllReplications();
  }

  protected void stopAllReplications() {
    for(Replication pullReplication : pullReplications.values()) {
      pullReplication.stop();
    }

    for(Replication pushReplication : pushReplications.values()) {
      pushReplication.stop();
    }
  }


  @Override
  public boolean isListenerStarted() {
    return couchbaseLiteListener != null;
  }

  @Override
  protected boolean startSynchronizationListener() {
    try {
      return startCBLListener(synchronizationPort, manager, allowedCredentials);
    } catch(Exception e) { log.error("Could not start Couchbase Lite synchronization listener", e); }

    return false;
  }

  @Override
  protected void stopSynchronizationListener() {
    stopCBLListener();
  }

  protected boolean startCBLListener(int listenPort, Manager manager, Credentials allowedCredentials) throws Exception {
    log.info("Starting Couchbase Lite Listener");

    couchbaseLiteListener = new LiteListener(manager, listenPort, allowedCredentials);
    synchronizationPort = couchbaseLiteListener.getListenPort();

    networkSettings.setSynchronizationPort(synchronizationPort);

    listenerThread = new Thread(couchbaseLiteListener);
    listenerThread.start();

    return synchronizationPort > 0 && synchronizationPort < 65536;
  }

  protected void stopCBLListener() {
    log.info("Stopping Couchbase Lite Listener");

    if(listenerThread != null) {
      try { listenerThread.join(500); } catch(Exception ignored) { }

      listenerThread = null;
    }

    if(couchbaseLiteListener != null) {
      couchbaseLiteListener.stop();
      couchbaseLiteListener = null;
    }
  }


  @Override
  protected void startSynchronizationWithDevice(DiscoveredDevice device) throws Exception {
    synchronized(this) {
      if(isListenerStarted() == false) { // first device has connected -> start Listener first
        startSynchronizationListener();
      }

      if(isAlreadySynchronizingWithDevice(device) == false) { // avoid that synchronization is started twice with the same device
        startReplication(device);
      }
    }
  }

  protected boolean isAlreadySynchronizingWithDevice(DiscoveredDevice device) {
    return pushReplications.containsKey(getDeviceKey(device));
  }

  protected String getDeviceKey(DiscoveredDevice device) {
    return device.getDevice().getUniqueDeviceId();
  }

  protected void startReplication(DiscoveredDevice device) throws Exception {
    log.info("Starting Replication with Device " + device);

    URL syncUrl;
    try {
      int remoteDeviceSyncPort = device.getSynchronizationPort();
      syncUrl = new URL("http://" + device.getAddress() + ":" + remoteDeviceSyncPort + "/" + database.getName());
    } catch (MalformedURLException e) {
      throw new Exception(e);
    }

    Replication pushReplication = database.createPushReplication(syncUrl);
    pushReplication.setFilter(FILTER_NAME);
    pushReplication.addChangeListener(replicationChangeListener);
    pushReplication.setContinuous(true);

    pushReplications.put(getDeviceKey(device), pushReplication);

    pushReplication.start();

    if (alsoUsePullReplication) {
      Replication pullReplication = database.createPullReplication(syncUrl);
      pullReplication.setFilter(FILTER_NAME);
      pullReplication.addChangeListener(replicationChangeListener);
      pullReplication.setContinuous(true);

      pullReplications.put(getDeviceKey(device), pullReplication);

      pullReplication.start();
    }

    database.addChangeListener(databaseChangeListener);
  }

  @Override
  protected void stopSynchronizationWithDevice(DiscoveredDevice device) {
    synchronized(this) {
      log.info("Stopping Replication with Device " + device);

      Replication pullReplication = pullReplications.remove(getDeviceKey(device));
      if(pullReplication != null) {
        pullReplication.stop();
      }

      Replication pushReplication = pushReplications.remove(getDeviceKey(device));
      if(pushReplication != null) {
        pushReplication.stop();
      }

      if(pushReplications.size() == 0) { // no devices connected anymore
        stopCBLListener();
      }
    }
  }



  protected Replication.ChangeListener replicationChangeListener = new Replication.ChangeListener() {
    @Override
    public void changed(Replication.ChangeEvent event) {

    }
  };

  protected Database.ChangeListener databaseChangeListener = new Database.ChangeListener() {
    @Override
    public void changed(final Database.ChangeEvent event) {
      if(event.isExternal()) {
        changeQueue.add(event);
      }
    }
  };


  protected ConsumerListener<Database.ChangeEvent> synchronizationChangesHandler = new ConsumerListener<Database.ChangeEvent>() {
    @Override
    public void consumeItem(Database.ChangeEvent item) {
      handleSynchronizedChanges(item.getChanges());
    }
  };


  protected void handleSynchronizedChanges(List<DocumentChange> changes) {
    for(DocumentChange change : changes) {
      Class<? extends BaseEntity> entityType = getEntityTypeFromDocumentChange(change);

      if(entityType != null) {
        handleChange(change, entityType);
      }
      else if(isEntityDeleted(change)) {
        handleDeletedEntity(change);
      }
    }
  }

  protected void handleChange(DocumentChange change, Class<? extends BaseEntity> entityType) {
    if(change.isConflict()) {
      conflictHandler.handleConflict(change, entityType);
    }

    BaseEntity synchronizedEntity = dataMerger.updateCachedSynchronizedEntity(change, entityType);
    if(synchronizedEntity == null) { // this entity is new to our side
      synchronizedEntity = entityManager.getEntityById(entityType, change.getDocumentId());
    }

    if(synchronizedEntity != null) {
      callEntitySynchronizedListeners(synchronizedEntity);
    }
  }


  protected void handleDeletedEntity(DocumentChange change) {
    String id = change.getDocumentId();
    Document document = entityManager.getDatabase().getDocument(id);
    if(document != null) {
      SavedRevision lastUndeletedRevision = findLastUndeletedRevision(document);

      if(lastUndeletedRevision != null) {
        Class entityType = getEntityTypeFromRevision(lastUndeletedRevision);
        if(entityType != null) {
          BaseEntity deletedEntity = entityManager.getEntityById(entityType, id);
          if(deletedEntity != null) {
            setDeletedProperty(deletedEntity, entityType);

            callEntitySynchronizedListeners(deletedEntity);
          }
        }
      }
    }
  }

  protected void setDeletedProperty(BaseEntity deletedEntity, Class entityType) {
    Dao dao = entityManager.getDaoForClass(entityType);

    if(dao != null) {
      for(PropertyConfig property : dao.getEntityConfig().getProperties()) {
        if(DatabaseTableConfig.BASE_ENTITY_DELETED_COLUMN_NAME.equals(property.getColumnName())) {
          try {
            dao.setValueOnObject(deletedEntity, property, true);
          } catch (Exception e) { log.error("Could not set deleted property on deleted entity " + deletedEntity, e); }

          break;
        }
      }
    }
  }

  protected SavedRevision findLastUndeletedRevision(Document document) {
    try {
      List<SavedRevision> leafRevisions = document.getLeafRevisions();
      if(leafRevisions.size() > 0) {
        String parentId = leafRevisions.get(0).getParentId();

        while(parentId != null) {
          SavedRevision parentRevision = document.getRevision(parentId);

          if(parentRevision.isDeletion() == false) {
            return parentRevision;
          }

          parentId = parentRevision.getParentId();
        }
      }
    } catch(Exception e) { log.error("Could not get Revision History for deleted Document with id " + document.getId(), e); }

    return null;
  }

  protected boolean isEntityDeleted(DocumentChange change) {
    return change.getAddedRevision().isDeleted();
  }

  protected Class<? extends BaseEntity> getEntityTypeFromRevision(SavedRevision revision) {
    String entityTypeString = (String)revision.getProperty(Dao.TYPE_COLUMN_NAME);

    return getEntityTypeFromEntityTypeString(entityTypeString);
  }

  protected Class<? extends BaseEntity> getEntityTypeFromDocumentChange(DocumentChange change) {
    String entityTypeString = (String)change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME);

    return getEntityTypeFromEntityTypeString(entityTypeString);
  }

  protected Class<? extends BaseEntity> getEntityTypeFromEntityTypeString(String entityTypeString) {
    Class<? extends BaseEntity> entityType = null;

    if(entityTypeString != null) { // sometimes there are documents without type or any other column/property except Couchbase's system properties (like _id)
      try {
        entityType = (Class<BaseEntity>) Class.forName(entityTypeString);
      } catch (Exception e) {
        log.error("Could not get class for entity type " + entityTypeString);
      }
    }

    return entityType;
  }

}
