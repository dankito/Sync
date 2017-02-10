package net.dankito.sync.di;

import android.content.Context;

import net.dankito.devicediscovery.IDevicesDiscoverer;
import net.dankito.devicediscovery.UdpDevicesDiscovererAndroid;
import net.dankito.sync.AndroidPlatformConfigurationReader;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.communication.CommunicationManager;
import net.dankito.sync.communication.IClientCommunicator;
import net.dankito.sync.communication.ICommunicationManager;
import net.dankito.sync.communication.TcpSocketClientCommunicator;
import net.dankito.sync.data.DataManager;
import net.dankito.sync.data.IDataManager;
import net.dankito.sync.data.IPlatformConfigurationReader;
import net.dankito.sync.devices.DevicesManager;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.devices.INetworkSettings;
import net.dankito.sync.devices.NetworkSettings;
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


  protected Localization localization = null;

  protected IThreadPool threadPool = null;

  protected IPlatformConfigurationReader platformConfigurationReader = null;

  protected IDataManager dataManager = null;

  protected IClientCommunicator clientCommunicator;

  protected ICommunicationManager communicationManager;

  protected IDevicesDiscoverer devicesDiscoverer = null;

  protected IDevicesManager devicesManager = null;

  protected IDataMerger dataMerger = null;

  protected FileSyncService fileSyncService = null;

  protected IFileStorageService fileStorageService = null;

  protected INetworkSettings networkSettings = null;

  protected EntityManagerConfiguration entityManagerConfiguration = null;

  protected IEntityManager entityManager = null;

  protected ISyncManager syncManager = null;

  protected ISyncConfigurationManager syncConfigurationManager = null;

  protected ISyncModuleConfigurationManager syncModuleConfigurationManager = null;


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
    if(localization == null) {
      localization = new Localization();
    }

    return localization;
  }

  @Provides
  @Singleton
  public IThreadPool provideThreadPool() {
    if(threadPool == null) {
      threadPool = new ThreadPool();
    }

    return threadPool;
  }


  @Provides
  @Singleton
  public IPlatformConfigurationReader providePlatformConfigurationReader() {
    if(platformConfigurationReader == null) {
      platformConfigurationReader = new AndroidPlatformConfigurationReader();
    }

    return platformConfigurationReader;
  }

  @Provides
  @Singleton
  public IDataManager provideDataManager(IEntityManager entityManager, IPlatformConfigurationReader platformConfigurationReader) {
    if(dataManager == null) {
      dataManager = new DataManager(entityManager, platformConfigurationReader);
    }

    return dataManager;
  }

  @Provides
  @Singleton
  public LocalConfig provideLocalConfig(IDataManager dataManager) {
    return dataManager.getLocalConfig();
  }


  @Provides
  @Singleton
  public IDevicesDiscoverer provideDevicesDiscoverer(IThreadPool threadPool) {
    if(devicesDiscoverer == null) {
      devicesDiscoverer = new UdpDevicesDiscovererAndroid(getContext(), threadPool);
    }

    return devicesDiscoverer;
  }

  @Provides
  @Singleton
  public IClientCommunicator provideClientCommunicator(INetworkSettings networkSettings, IThreadPool threadPool) {
    if(clientCommunicator == null) {
      clientCommunicator = new TcpSocketClientCommunicator(networkSettings, threadPool);
    }

    return clientCommunicator;
  }

  @Provides
  @Singleton
  public ICommunicationManager provideCommunicationManager(IDevicesManager devicesManager, IClientCommunicator clientCommunicator, INetworkSettings networkSettings) {
    if(communicationManager == null) {
      communicationManager = new CommunicationManager(devicesManager, clientCommunicator, networkSettings);
    }

    return communicationManager;
  }

  @Provides
  @Singleton
  public IDevicesManager provideDevicesManager(IDevicesDiscoverer devicesDiscoverer, IClientCommunicator clientCommunicator, IDataManager dataManager, INetworkSettings networkSettings, IEntityManager entityManager) {
    if(devicesManager == null) {
      devicesManager = new DevicesManager(devicesDiscoverer, clientCommunicator, dataManager, networkSettings, entityManager);
    }

    return devicesManager;
  }

  @Provides
  @Singleton
  public IDataMerger provideDataMerger(IEntityManager entityManager) {
    if(dataMerger == null) {
      dataMerger = new JpaMetadataBasedDataMerger((CouchbaseLiteEntityManagerBase)entityManager);
    }

    return dataMerger;
  }

  @Provides
  @Singleton
  public FileSyncService provideFileSyncService(IEntityManager entityManager) {
    if(fileSyncService == null) {
      fileSyncService = new FileSyncService(entityManager);
    }

    return fileSyncService;
  }

  @Provides
  @Singleton
  public IFileStorageService provideFileStorageService() {
    if(fileStorageService == null) {
      fileStorageService = new JavaFileStorageService();
    }

    return fileStorageService;
  }

  @Provides
  @Singleton
  public INetworkSettings provideNetworkNetworkSettings(IDataManager dataManager) {
    if(networkSettings == null) {
      networkSettings = new NetworkSettings(dataManager);
    }

    return networkSettings;
  }


  @Provides
  @Singleton
  public EntityManagerConfiguration provideEntityManagerConfiguration() {
    if(entityManagerConfiguration == null) {
      entityManagerConfiguration = new EntityManagerConfiguration(EntityManagerDefaultConfiguration.DEFAULT_DATA_FOLDER, EntityManagerDefaultConfiguration.APPLICATION_DATA_MODEL_VERSION);
    }

    return entityManagerConfiguration;
  }

  @Provides
  @Singleton
  public IEntityManager provideEntityManager(EntityManagerConfiguration configuration) throws RuntimeException {
    if(entityManager == null) {
      try {
          entityManager = new CouchbaseLiteEntityManagerAndroid(getContext(), configuration);
      } catch(Exception e) {
        // TODO: what to do in error case? (should actually never occur, but if?)
      }
    }

    return entityManager;
  }


  @Provides
  @Singleton
  public ISyncManager provideSyncManager(IEntityManager entityManager, INetworkSettings networkSettings, IDevicesManager devicesManager, IThreadPool threadPool) {
    if(syncManager == null) {
      syncManager = new CouchbaseLiteSyncManager((CouchbaseLiteEntityManagerBase)entityManager, networkSettings, devicesManager, threadPool,
          SynchronizationConfig.DEFAULT_SYNCHRONIZATION_PORT, SynchronizationConfig.DEFAULT_ALSO_USE_PULL_REPLICATION);
    }

    return syncManager;
  }

  @Provides
  @Singleton
  public ISyncConfigurationManager provideSyncConfigurationManager(Localization localization, ISyncManager syncManager, IDataManager dataManager, IEntityManager entityManager, IDevicesManager devicesManager,
                                                                   IDataMerger dataMerger, FileSyncService fileSyncService, IFileStorageService fileStorageService, IThreadPool threadPool) {
    if(syncConfigurationManager == null) {
      syncConfigurationManager = new SyncConfigurationManagerAndroid(getContext(), localization, syncManager, dataManager, entityManager, devicesManager, dataMerger,
          fileSyncService, fileStorageService, threadPool);
    }

    return syncConfigurationManager;
  }

  @Provides
  @Singleton
  public ISyncModuleConfigurationManager provideSyncModuleConfigurationManager(ISyncConfigurationManager syncConfigurationManager, IEntityManager entityManager, IDataManager dataManager) {
    if(syncModuleConfigurationManager == null) {
      syncModuleConfigurationManager = new SyncModuleConfigurationManager(syncConfigurationManager, entityManager, dataManager);
    }

    return syncModuleConfigurationManager;
  }

}
