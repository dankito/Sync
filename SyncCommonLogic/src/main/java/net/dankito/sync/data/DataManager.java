package net.dankito.sync.data;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.OsType;
import net.dankito.sync.persistence.IEntityManager;

import java.util.List;
import java.util.UUID;

import javax.inject.Named;


@Named
public class DataManager implements IDataManager {

  protected IEntityManager entityManager;

  protected IPlatformConfigurationReader platformConfigurationReader;

  protected LocalConfig localConfig;

  protected Device localDevice;


  public DataManager(IEntityManager entityManager, IPlatformConfigurationReader platformConfigurationReader) {
    this.entityManager = entityManager;
    this.platformConfigurationReader = platformConfigurationReader;

    readConfigurationFromDb();
  }


  protected void readConfigurationFromDb() {
    List<LocalConfig> localConfigs = entityManager.getAllEntitiesOfType(LocalConfig.class);

    if(localConfigs.size() == 0) { // first app start
      setupInitialApplicationConfiguration();
    }
    else {
      this.localConfig = localConfigs.get(0); // TODO: what to do if localConfigs.size() > 1?
      this.localDevice = localConfig.getLocalDevice();
    }
  }

  protected void setupInitialApplicationConfiguration() {
    this.localDevice = new Device(UUID.randomUUID().toString());

    localDevice.setName(platformConfigurationReader.getDeviceName());
    localDevice.setOsType(parseOsType(platformConfigurationReader));
    localDevice.setOsName(platformConfigurationReader.getPlatformName());
    localDevice.setOsVersion(platformConfigurationReader.getOsVersion());

    this.localConfig = new LocalConfig(localDevice);

    entityManager.persistEntity(localConfig);
  }

  protected OsType parseOsType(IPlatformConfigurationReader platformConfigurationReader) {
    String platformName = platformConfigurationReader.getPlatformName();

    if(platformName.toLowerCase().contains("android")) {
      return OsType.ANDROID;
    }

    return OsType.DESKTOP;
  }


  @Override
  public LocalConfig getLocalConfig() {
    return localConfig;
  }

}
