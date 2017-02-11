package net.dankito.sync.synchronization;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ganymed on 28/08/16.
 */
public abstract class SyncManagerBase implements ISyncManager {

  private static final Logger log = LoggerFactory.getLogger(SyncManagerBase.class);


  protected IThreadPool threadPool;

  protected Set<SynchronizationListener> synchronizationListeners = new HashSet<>();


  public SyncManagerBase(IThreadPool threadPool) {
    this.threadPool = threadPool;
  }


  protected abstract boolean isListenerStarted();

  protected abstract boolean startSynchronizationListener();

  protected abstract void stopSynchronizationListener();

  protected abstract void startSynchronizationWithDevice(DiscoveredDevice device) throws Exception;

  protected abstract void stopSynchronizationWithDevice(DiscoveredDevice device);


  protected void startSynchronizationWithDeviceAsync(final DiscoveredDevice device) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        try {
          startSynchronizationWithDevice(device);
        } catch(Exception e) {
          log.error("Could not start Synchronization with Device " + device); // TODO: inform User (e.g. over a Notification)
        }
      }
    });
  }


  protected boolean hasSynchronizationListeners() {
    return synchronizationListeners.size() > 0;
  }

  public boolean addSynchronizationListener(SynchronizationListener listener) {
    return synchronizationListeners.add(listener);
  }

  public boolean removeSynchronizationListener(SynchronizationListener listener) {
    return synchronizationListeners.remove(listener);
  }

  protected void callEntitySynchronizedListeners(BaseEntity entity) {
    for(SynchronizationListener listener : synchronizationListeners) {
      listener.entitySynchronized(entity);
    }
  }

}
