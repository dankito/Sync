package net.dankito.sync.di;

import android.app.Activity;

import net.dankito.android.util.AlertHelper;
import net.dankito.android.util.AndroidOnUiThreadRunner;
import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.devicediscovery.UdpDevicesDiscovererAndroid;
import net.dankito.sync.devices.DevicesManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.INetworkConfigurationManager;
import net.dankito.sync.devices.NetworkConfigurationManager;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerAndroid;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.EntityManagerDefaultConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.CouchbaseLiteSyncManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SynchronizationConfig;
import net.dankito.utils.IOnUiThreadRunner;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ganymed on 03/11/16.
 */
@Module
public class AndroidDiContainer {

  protected final Activity activity;


  public AndroidDiContainer (Activity activity) {
    this.activity = activity;
  }


  @Provides //scope is not necessary for parameters stored within the module
  public Activity getActivity() {
    return activity;
  }


  @Provides
  @Singleton
  public IThreadPool provideThreadPool() {
    return new ThreadPool();
  }

  @Provides
  @Singleton
  public IOnUiThreadRunner provideOnUiThreadRunner() {
    return new AndroidOnUiThreadRunner(getActivity());
  }


  @Provides
  @Singleton
  public IDevicesDiscoverer provideDevicesDiscoverer(IThreadPool threadPool) {
    return new UdpDevicesDiscovererAndroid(getActivity(), threadPool);
  }

  @Provides
  @Singleton
  public IDevicesManager provideDevicesManager(IDevicesDiscoverer devicesDiscoverer) {
    return new DevicesManager(devicesDiscoverer, null); // TODO: provide localDevice
  }

  @Provides
  @Singleton
  public INetworkConfigurationManager provideNetworkConfigurationManager() {
    return new NetworkConfigurationManager();
  }


  @Provides
  @Singleton
  public EntityManagerConfiguration provideEntityManagerConfiguration() {
    return new EntityManagerConfiguration(EntityManagerDefaultConfiguration.DEFAULT_DATA_FOLDER, EntityManagerDefaultConfiguration.APPLICATION_DATA_MODEL_VERSION);
  }

  @Provides
  @Singleton
  public IEntityManager provideEntityManager(EntityManagerConfiguration configuration) throws RuntimeException {
    try {
      return new CouchbaseLiteEntityManagerAndroid(getActivity(), configuration);
    } catch(Exception e) {
      AlertHelper.showErrorMessageThreadSafe(getActivity(), "Could not load Database, it is better closing Application now: " + e.getLocalizedMessage()); // TODO: translate
    }

    return null;
  }


  @Provides
  @Singleton
  public ISyncManager provideSyncManager(IEntityManager entityManager, INetworkConfigurationManager networkConfigurationManager, IDevicesManager devicesManager, IThreadPool threadPool) {
    return new CouchbaseLiteSyncManager((CouchbaseLiteEntityManagerBase)entityManager, networkConfigurationManager, devicesManager, threadPool,
        SynchronizationConfig.DEFAULT_SYNCHRONIZATION_PORT, SynchronizationConfig.DEFAULT_ALSO_USE_PULL_REPLICATION);
  }

}
