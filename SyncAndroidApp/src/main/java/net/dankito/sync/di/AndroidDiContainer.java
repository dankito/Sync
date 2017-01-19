package net.dankito.sync.di;

import android.app.Activity;

import net.dankito.android.util.AlertHelper;
import net.dankito.android.util.AndroidOnUiThreadRunner;
import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.PermissionsManager;
import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.devicediscovery.UdpDevicesDiscovererAndroid;
import net.dankito.sync.AndroidPlatformConfigurationReader;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.data.DataManager;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.data.IPlatformConfigurationReader;
import net.dankito.sync.devices.DevicesManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.INetworkConfigurationManager;
import net.dankito.sync.devices.NetworkConfigurationManager;
import net.dankito.sync.localization.Localization;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerAndroid;
import net.dankito.sync.persistence.CouchbaseLiteEntityManagerBase;
import net.dankito.sync.persistence.EntityManagerConfiguration;
import net.dankito.sync.persistence.EntityManagerDefaultConfiguration;
import net.dankito.sync.persistence.IEntityManager;
import net.dankito.sync.synchronization.CouchbaseLiteSyncManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;
import net.dankito.sync.synchronization.ISyncManager;
import net.dankito.sync.synchronization.SyncConfigurationManagerAndroid;
import net.dankito.sync.synchronization.SynchronizationConfig;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.merge.JpaMetadataBasedDataMerger;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncModuleConfigurationManager;
import net.dankito.utils.IOnUiThreadRunner;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.IFileStorageService;
import net.dankito.utils.services.JavaFileStorageService;

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
  public Localization provideLocalization() {
    return new Localization();
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
  public IPermissionsManager providePermissionsManager() {
    return new PermissionsManager(getActivity());
  }


  @Provides
  @Singleton
  public IPlatformConfigurationReader providePlatformConfigurationReader() {
    return new AndroidPlatformConfigurationReader();
  }

  @Provides
  @Singleton
  public IDataManager provideDataManager(IEntityManager entityManager, IPlatformConfigurationReader platformConfigurationReader) {
    return new DataManager(entityManager, platformConfigurationReader);
  }

  @Provides
  @Singleton
  public LocalConfig provideLocalConfig(IDataManager dataManager) {
    return dataManager.getLocalConfig();
  }


  @Provides
  @Singleton
  public IDevicesDiscoverer provideDevicesDiscoverer(IThreadPool threadPool) {
    return new UdpDevicesDiscovererAndroid(getActivity(), threadPool);
  }

  @Provides
  @Singleton
  public IDevicesManager provideDevicesManager(IDevicesDiscoverer devicesDiscoverer, IDataManager dataManager, IEntityManager entityManager) {
    return new DevicesManager(devicesDiscoverer, dataManager, entityManager);
  }

  @Provides
  @Singleton
  public IDataMerger provideDataMerger(IEntityManager entityManager) {
    return new JpaMetadataBasedDataMerger((CouchbaseLiteEntityManagerBase)entityManager);
  }

  @Provides
  @Singleton
  public IFileStorageService provideFileStorageService() {
    return new JavaFileStorageService();
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

  @Provides
  @Singleton
  public ISyncConfigurationManager provideSyncConfigurationManager(IPermissionsManager permissionsManager, Localization localization, ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                                                   IDataMerger dataMerger, IFileStorageService fileStorageService, IThreadPool threadPool) {
    return new SyncConfigurationManagerAndroid(getActivity(), localization, permissionsManager, syncManager, dataManager, entityManager, devicesManager, dataMerger,
        fileStorageService, threadPool);
  }

  @Provides
  @Singleton
  public ISyncModuleConfigurationManager provideSyncModuleConfigurationManager(ISyncConfigurationManager syncConfigurationManager, IEntityManager entityManager, IDataManager dataManager) {
    return new SyncModuleConfigurationManager(syncConfigurationManager, entityManager, dataManager);
  }

}
