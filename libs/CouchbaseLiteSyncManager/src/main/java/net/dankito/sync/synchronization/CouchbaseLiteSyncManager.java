package net.dankito.sync.synchronization;

import com.couchbase.lite.Database;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.sync.BaseEntity;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncEntityLocalLookUpKeys;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.INetworkConfigurationManager;
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


public class CouchbaseLiteSyncManager extends SyncManagerBase {

  protected static final String FILTER_NAME = "ENTITIES_FILTER";

  protected static final int MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY = 2500;


  private static final Logger log = LoggerFactory.getLogger(CouchbaseLiteSyncManager.class);


  protected CouchbaseLiteEntityManagerBase entityManager;

  protected Database database;

  protected Manager manager;

  protected INetworkConfigurationManager configurationManager;

  protected boolean alsoUsePullReplication;

  protected int synchronizationPort;
  protected Credentials allowedCredentials;

  protected Thread listenerThread;

  protected LiteListener couchbaseLiteListener;

  protected Map<String, Replication> pushReplications = new ConcurrentHashMap<>();
  protected Map<String, Replication> pullReplications = new ConcurrentHashMap<>();

  protected AsyncProducerConsumerQueue<Database.ChangeEvent> changeQueue;


  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, INetworkConfigurationManager configurationManager, IDevicesManager devicesManager, IThreadPool threadPool) {
    this(entityManager, configurationManager, devicesManager, threadPool, SynchronizationConfig.DEFAULT_SYNCHRONIZATION_PORT, true);
  }

  public CouchbaseLiteSyncManager(CouchbaseLiteEntityManagerBase entityManager, INetworkConfigurationManager configurationManager, IDevicesManager devicesManager,
                                  IThreadPool threadPool, int synchronizationPort, boolean alsoUsePullReplication) {
    super(devicesManager, threadPool);
    this.entityManager = entityManager;
    this.database = entityManager.getDatabase();
    this.manager = database.getManager();
    this.configurationManager = configurationManager;
    this.synchronizationPort = synchronizationPort;
    this.alsoUsePullReplication = alsoUsePullReplication;

    // wait some time before processing synchronized entities as they may have dependent entities which haven't been synchronized yet
    this.changeQueue = new AsyncProducerConsumerQueue<>(1, MILLIS_TO_WAIT_BEFORE_PROCESSING_SYNCHRONIZED_ENTITY, synchronizationChangesHandler);

    setReplicationFilter(database);
  }

  private void setReplicationFilter(Database database) {
    final List<String> entitiesToFilter = new ArrayList<>();
    entitiesToFilter.add(LocalConfig.class.getName());
    entitiesToFilter.add(SyncEntityLocalLookUpKeys.class.getName());

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
  protected void startSynchronizationListener() {
    try {
      startCBLListener(synchronizationPort, manager, allowedCredentials);
    } catch(Exception e) { log.error("Could not start Couchbase Lite synchronization listener", e); }
  }

  @Override
  protected void stopSynchronizationListener() {
    stopCBLListener();
  }

  protected void startCBLListener(int listenPort, Manager manager, Credentials allowedCredentials) throws Exception {
    log.info("Starting Couchbase Lite Listener");

    couchbaseLiteListener = new LiteListener(manager, listenPort, allowedCredentials);
    synchronizationPort = couchbaseLiteListener.getListenPort();

    configurationManager.setSynchronizationPort(synchronizationPort);

    listenerThread = new Thread(couchbaseLiteListener);
    listenerThread.start();
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
        BaseEntity synchronizedEntity = getEntityFromDocumentChange(change, entityType);

        if(change.isConflict()) {
          handleConflict(change, entityType);
        }

        callEntitySynchronizedListeners(synchronizedEntity);
      }
    }
  }

  protected Class<? extends BaseEntity> getEntityTypeFromDocumentChange(DocumentChange change) {
    String entityTypeString = (String)change.getAddedRevision().getPropertyForKey(Dao.TYPE_COLUMN_NAME);

    Class<? extends BaseEntity> entityType = null;
    try {
      entityType = (Class<BaseEntity>)Class.forName(entityTypeString);
    } catch(Exception e) {
      log.error("Could not get class for entity type " + entityTypeString);
    }

    return entityType;
  }

  protected BaseEntity getEntityFromDocumentChange(DocumentChange change, Class<? extends BaseEntity> entityType) {
    String id = (String)change.getAddedRevision().getPropertyForKey(Dao.ID_COLUMN_NAME);

    return entityManager.getEntityById(entityType, id);
  }


  protected void handleConflict(DocumentChange change, Class<? extends BaseEntity> entityType) {
    // TODO
  }

}
