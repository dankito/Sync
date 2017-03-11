package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.SyncEntityChange;
import net.dankito.sync.synchronization.SyncEntityChangeListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class SyncModuleBase implements ISyncModule {

  public static final int DISPLAY_PRIORITY_HIGHEST = 1;

  public static final int DISPLAY_PRIORITY_HIGH = 10;

  public static final int DISPLAY_PRIORITY_MEDIUM = 100;

  public static final int DISPLAY_PRIORITY_LOW = 1000;

  public static final int DISPLAY_PRIORITY_LOWEST = 10000;


  protected Localization localization;

  protected List<SyncEntityChangeListener> listeners = new CopyOnWriteArrayList<>();

  protected List<ISyncModule> linkedSyncModules = new CopyOnWriteArrayList<>();


  public SyncModuleBase(Localization localization) {
    this.localization = localization;
  }


  protected abstract String getNameStringResourceKey();


  public String getName() {
    return localization.getLocalizedString(getNameStringResourceKey());
  }

  @Override
  public void readAllEntitiesAsync(ReadEntitiesCallback callback) {
    for(ISyncModule linkedSyncModule : linkedSyncModules) {
      linkedSyncModule.readAllEntitiesAsync(callback);
    }
  }


  @Override
  public boolean deleteSyncEntityProperty(SyncEntity entity, SyncEntity property) {
    return false; // true for most SyncModules
  }

  @Override
  public void configureLocalSynchronizationSettings(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    // nothing to do more most SyncModules
  }


  @Override
  public void handleRetrievedSynchronizedEntityAsync(SyncJobItem jobItem, SyncEntityState entityState, HandleRetrievedSynchronizedEntityCallback callback) {
    for(ISyncModule linkedSyncModule : linkedSyncModules) {
      linkedSyncModule.handleRetrievedSynchronizedEntityAsync(jobItem, entityState, callback);
    }
  }


  @Override
  public void addSyncEntityChangeListener(SyncEntityChangeListener listener) {
    synchronized(listeners) {
      listeners.add(listener);

      listenerAdded(listener, listeners);
    }
  }

  protected void listenerAdded(SyncEntityChangeListener addedListener, List<SyncEntityChangeListener> allListeners) {
    // maybe overwritten in sub classes
  }

  @Override
  public void removeSyncEntityChangeListener(SyncEntityChangeListener listener) {
    synchronized(listeners) {
      listeners.remove(listener);

      listenerRemoved(listener, listeners);
    }
  }

  protected void listenerRemoved(SyncEntityChangeListener removedListener, List<SyncEntityChangeListener> allListeners) {
    // maybe overwritten in sub classes
  }

  protected void callSyncEntityChangeListeners(SyncEntityChange change) {
    for(SyncEntityChangeListener listener : listeners) {
      listener.entityChanged(change);
    }
  }


  @Override
  public boolean registerLinkedSyncModule(ISyncModule syncModule) {
    return linkedSyncModules.add(syncModule);
  }

  @Override
  public boolean unregisterLinkedSyncModule(ISyncModule syncModule) {
    return linkedSyncModules.remove(syncModule);
  }


  @Override
  public String toString() {
    return getName();
  }

}
