package net.dankito.sync.di;

import android.content.Context;

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
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.sync.synchronization.merge.IDataMerger;
import net.dankito.sync.synchronization.merge.JpaMetadataBasedDataMerger;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncModuleConfigurationManager;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;
import net.dankito.utils.services.IFileStorageService;
import net.dankito.utils.services.JavaFileStorageService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AndroidServiceDiContainer {

  protected final Context context;


  public AndroidServiceDiContainer(Context context) {
    this.context = context;
  }


  @Provides //scope is not necessary for parameters stored within the module
  public Context getContext() {
    return context;
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
    return new UdpDevicesDiscovererAndroid(getContext(), threadPool);
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
  public FileSyncService provideFileSyncService(IEntityManager entityManager) {
    return new FileSyncService(entityManager);
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
      return new CouchbaseLiteEntityManagerAndroid(getContext(), configuration);
    } catch(Exception e) {
      // TODO: what to do in error case? (should actually never occur, but if?)
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
  public ISyncConfigurationManager provideSyncConfigurationManager(Localization localization, ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                                                   IDataMerger dataMerger, FileSyncService fileSyncService, IFileStorageService fileStorageService, IThreadPool threadPool) {
    return new SyncConfigurationManagerAndroid(getContext(), localization, syncManager, dataManager, entityManager, devicesManager, dataMerger,
        fileSyncService, fileStorageService, threadPool);
  }

  @Provides
  @Singleton
  public ISyncModuleConfigurationManager provideSyncModuleConfigurationManager(ISyncConfigurationManager syncConfigurationManager, IEntityManager entityManager, IDataManager dataManager) {
    return new SyncModuleConfigurationManager(syncConfigurationManager, entityManager, dataManager);
  }

}
