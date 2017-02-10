package net.dankito.sync.synchronization;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
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


  public SyncManagerBase(IDevicesManager devicesManager, IThreadPool threadPool) {
    this.threadPool = threadPool;

    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);
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


  // TODO: this is wrong, could also be an unregistered device
  protected DiscoveredDevicesListener discoveredDevicesListener = new DiscoveredDevicesListener() {
    @Override
    public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
      if(isListenerStarted() == false) {
        startSynchronizationListener();
      }

      if(type == DiscoveredDeviceType.KNOWN_SYNCHRONIZED_DEVICE || isSyncDestinationForRemoteDevice(connectedDevice)) {
        startSynchronizationWithDeviceAsync(connectedDevice);
      }
    }

    @Override
    public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {

    }
  };

  protected boolean isSyncDestinationForRemoteDevice(DiscoveredDevice remoteDevice) {
    // TODO
//    for(SyncConfiguration syncConfiguration : remoteDevice.getDevice().getSourceSyncConfigurations()) {
//      if()
//    }

    return true;
  }

}
