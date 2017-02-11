package net.dankito.sync.synchronization;

import net.dankito.sync.BaseEntity;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.devices.NetworkSetting;
import net.dankito.sync.devices.NetworkSettingsChangedListener;
import net.dankito.utils.IThreadPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


public abstract class SyncManagerBase implements ISyncManager {

  private static final Logger log = LoggerFactory.getLogger(SyncManagerBase.class);


  protected IThreadPool threadPool;

  protected Set<SynchronizationListener> synchronizationListeners = new HashSet<>();


  public SyncManagerBase(INetworkSettings networkSettings, IThreadPool threadPool) {
    this.threadPool = threadPool;

    networkSettings.addListener(networkSettingsChangedListener);
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


  protected NetworkSettingsChangedListener networkSettingsChangedListener = new NetworkSettingsChangedListener() {
    @Override
    public void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue) {
      if(setting == NetworkSetting.ADDED_CONNECTED_DEVICE_PERMITTED_TO_SYNCHRONIZE) {
        synchronizingWithDevicePermitted((DiscoveredDevice)newValue);
      }
      else if(setting == NetworkSetting.REMOVED_CONNECTED_DEVICE_PERMITTED_TO_SYNCHRONIZE) {
        stoppedSynchronizingWithDevice((DiscoveredDevice)newValue);
      }
    }
  };

  protected void synchronizingWithDevicePermitted(DiscoveredDevice device) {
    if(isListenerStarted() == false) {
      startSynchronizationListener();
    }

    startSynchronizationWithDeviceAsync(device);
  }

  protected void stoppedSynchronizingWithDevice(DiscoveredDevice device) {

    stopSynchronizationWithDevice(device);
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
