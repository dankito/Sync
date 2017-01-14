package net.dankito.sync.persistence;

import javax.inject.Named;


@Named
public class JavaEntityManagerConfiguration extends EntityManagerConfiguration {

  public JavaEntityManagerConfiguration() {
    this(EntityManagerDefaultConfiguration.DEFAULT_DATA_FOLDER, EntityManagerDefaultConfiguration.APPLICATION_DATA_MODEL_VERSION);
  }

  public JavaEntityManagerConfiguration(String dataFolder, int applicationDataModelVersion) {
    super(dataFolder, applicationDataModelVersion);
  }

}
