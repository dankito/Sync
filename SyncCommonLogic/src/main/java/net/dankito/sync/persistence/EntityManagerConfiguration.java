package net.dankito.sync.persistence;

import net.dankito.sync.CallLogSyncEntity;
import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.Device;
import net.dankito.sync.FileSyncEntity;
import net.dankito.sync.ImageFileSyncEntity;
import net.dankito.sync.LocalConfig;
import net.dankito.sync.SyncConfiguration;
import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityLocalLookUpKeys;
import net.dankito.sync.SyncJobItem;
import net.dankito.sync.SyncModuleConfiguration;

import java.io.File;

/**
 * Created by ganymed on 03/01/15.
 */
public class EntityManagerConfiguration {

  protected Class<? extends IEntityManager> entityManagerClass;
  
  protected Class[] entityClasses = new Class[0];

  protected String dataFolder = null;
  protected String dataCollectionFileName = null;
  protected String dataCollectionPersistencePath = null;

  protected int dataBaseCurrentDataModelVersion;
  protected int applicationDataModelVersion;

  protected boolean createDatabase = false;
  protected boolean createTables = false;

  protected String databaseDriverUrl = null;
  protected String databaseDriver = null;

  protected boolean dropTables = false;



  public EntityManagerConfiguration(String dataFolder, int applicationDataModelVersion) {
    this(dataFolder, applicationDataModelVersion, false);
  }

  public EntityManagerConfiguration(String dataFolder, int applicationDataModelVersion, boolean createTables) {
    setDataFolder(dataFolder);
    setApplicationDataModelVersion(applicationDataModelVersion);

    setDatabaseConfiguration(createTables);

    addEntities();
  }


  public Class<? extends IEntityManager> getEntityManagerClass() {
    return entityManagerClass;
  }

  public void setEntityManagerClass(Class<? extends IEntityManager> entityManagerClass) {
    this.entityManagerClass = entityManagerClass;
  }

  public Class[] getEntityClasses() {
    return entityClasses;
  }

  public void setEntityClasses(Class[] entityClasses) {
    this.entityClasses = entityClasses;
  }

  public String getDataFolder() {
    return dataFolder;
  }

  public void setDataFolder(String dataFolder) {
    this.dataFolder = dataFolder;
    this.dataCollectionPersistencePath = dataFolder + dataCollectionFileName;
  }

  public String getDataCollectionFileName() {
    return dataCollectionFileName;
  }

  public void setDataCollectionFileName(String dataCollectionFileName) {
    this.dataCollectionFileName = dataCollectionFileName;
    this.dataCollectionPersistencePath = dataFolder + dataCollectionFileName;
  }

  public String getDataCollectionPersistencePath() {
    if(databaseDriver != null && databaseDriver.toLowerCase().contains("h2") && dataCollectionPersistencePath.endsWith(".mv.db") == false)
      return dataCollectionPersistencePath + ".mv.db"; // H2 adds a '.mv.db' at end of file path
    return dataCollectionPersistencePath;
  }

  public void setDataCollectionPersistencePath(String dataCollectionPersistencePath) {
    this.dataCollectionPersistencePath = dataCollectionPersistencePath;
  }

  public int getDataBaseCurrentDataModelVersion() {
    return dataBaseCurrentDataModelVersion;
  }

  public void setDataBaseCurrentDataModelVersion(int dataBaseCurrentDataModelVersion) {
    this.dataBaseCurrentDataModelVersion = dataBaseCurrentDataModelVersion;
  }

  public int getApplicationDataModelVersion() {
    return applicationDataModelVersion;
  }

  public void setApplicationDataModelVersion(int applicationDataModelVersion) {
    this.applicationDataModelVersion = applicationDataModelVersion;
  }

  public boolean createDatabase() {
    return createDatabase;
  }

  public void setCreateDatabase(boolean createDatabase) {
    this.createDatabase = createDatabase;
  }

  public String getDatabaseDriverUrl() {
    return databaseDriverUrl;
  }

  public void setDatabaseDriverUrl(String databaseDriverUrl) {
    this.databaseDriverUrl = databaseDriverUrl;
  }

  public String getDatabaseDriver() {
    return databaseDriver;
  }

  public void setDatabaseDriver(String databaseDriver) {
    this.databaseDriver = databaseDriver;
  }

//  public String getDdlGeneration() {
//    return ddlGeneration;
//  }
//
//  public void setDdlGeneration(String ddlGeneration) {
//    this.ddlGeneration = ddlGeneration;
//  }


  public boolean createTables() {
    return createTables;
  }

  public void setCreateTables(boolean createTables) {
    this.createTables = createTables;
  }

  public boolean dropTables() {
    return dropTables;
  }

  public void setDropTables(boolean dropTables) {
    this.dropTables = dropTables;
  }


  protected void setDatabaseConfiguration( boolean createTables) {
    setDataCollectionFileName("sync_db"); // TODO: configure
    setCreateTables(createTables);

    if(new File(getDataCollectionPersistencePath()).exists() == false) {
      setCreateDatabase(true);
      setCreateTables(true);
    }
  }


  protected void addEntities() {
    Class[] entities = new Class[] {

        LocalConfig.class,

        Device.class,

        SyncEntity.class,
        CallLogSyncEntity.class,
        ContactSyncEntity.class,
        FileSyncEntity.class,
        ImageFileSyncEntity.class,

        SyncConfiguration.class,
        SyncModuleConfiguration.class,
        SyncJobItem.class,
        SyncEntityLocalLookUpKeys.class

    };
    
    setEntityClasses(entities);
  }

}
