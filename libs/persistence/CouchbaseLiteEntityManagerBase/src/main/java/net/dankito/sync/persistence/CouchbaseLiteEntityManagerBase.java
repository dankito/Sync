package net.dankito.sync.persistence;

import com.couchbase.lite.Context;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ManagerOptions;

import net.dankito.jpa.annotationreader.JpaAnnotationReader;
import net.dankito.jpa.annotationreader.JpaAnnotationReaderResult;
import net.dankito.jpa.annotationreader.config.EntityConfig;
import net.dankito.jpa.cache.DaoCache;
import net.dankito.jpa.cache.ObjectCache;
import net.dankito.jpa.couchbaselite.Dao;
import net.dankito.jpa.util.DatabaseCompacter;
import net.dankito.sync.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CouchbaseLiteEntityManagerBase implements IEntityManager {

  protected static final int MINIMUM_DELAY_BETWEEN_TWO_COMPACT_RUNS_MILLIS = 10 * 1000; // at maximum each 10 seconds

  private static final Logger log = LoggerFactory.getLogger(CouchbaseLiteEntityManagerBase.class);


  protected Manager manager;

  protected Context context;

  protected Database database;

  protected DatabaseCompacter databaseCompacter;

  protected DaoCache daoCache = new DaoCache();

  protected ObjectCache objectCache = new ObjectCache();

  protected String databasePath = null;

  protected Map<Class, Dao> mapEntityClassesToDaos = new HashMap<>();


  public CouchbaseLiteEntityManagerBase(Context context, EntityManagerConfiguration configuration) throws Exception {
    this.context = context;
    this.databasePath = adjustDatabasePath(context, configuration);

    // TODO: read current database data model version

    createDatabase(configuration);

    JpaAnnotationReader jpaAnnotationReader = new JpaAnnotationReader();
    JpaAnnotationReaderResult result = jpaAnnotationReader.readConfiguration(configuration.getEntityClasses());

    createDaos(result);

    if (configuration.getDataBaseCurrentDataModelVersion() < configuration.getApplicationDataModelVersion()) {
      // TODO: update DatabaseDataModelVersion
//      Application.getPreferencesStore().setDatabaseDataModelVersion(configuration.getApplicationDataModelVersion());
    }
  }

  protected abstract String adjustDatabasePath(Context context, EntityManagerConfiguration configuration);

  protected void createDatabase(EntityManagerConfiguration configuration) throws CouchbaseLiteException, IOException {
    ManagerOptions managerOptions = Manager.DEFAULT_OPTIONS;
    manager = new Manager(context, managerOptions);

    DatabaseOptions options = new DatabaseOptions();
    options.setCreate(true);

    database = manager.openDatabase(configuration.getDataCollectionFileName(), options);

    databaseCompacter = new DatabaseCompacter(database, MINIMUM_DELAY_BETWEEN_TWO_COMPACT_RUNS_MILLIS);
  }

  protected void createDaos(JpaAnnotationReaderResult result) {
    for(EntityConfig entityConfig : result.getReadEntities()) {
      Dao entityDao = createDaoForEntity(entityConfig);

      daoCache.addDao(entityConfig.getEntityClass(), entityDao);
      mapEntityClassesToDaos.put(entityConfig.getEntityClass(), entityDao);
    }
  }

  protected Dao createDaoForEntity(EntityConfig entityConfig) {
    return new Dao(database, entityConfig, objectCache, daoCache, databaseCompacter);
  }


  @Override
  public String getDatabasePath() {
    return databasePath;
  }

  @Override
  public boolean persistEntity(BaseEntity entity) {
    try {
      Dao dao = getDaoForEntity(entity);

      if(dao != null) {
        return dao.create(entity);
      }
    }
    catch(Exception e) { log.error("Could not create entity " + entity, e); }

    return false;
  }

  @Override
  public boolean updateEntity(BaseEntity entity) {
    try {
      Dao dao = getDaoForEntity(entity);

      if(dao != null) {
        return dao.update(entity);
      }
    }
    catch(Exception e) { log.error("Could not update entity " + entity, e); }

    return false;
  }

  @Override
  public boolean updateEntities(List<BaseEntity> entities) {
    boolean result = true;

    for(BaseEntity entity : entities) {
      result &= updateEntity(entity);
    }

    return result;
  }

  @Override
  public boolean deleteEntity(BaseEntity entity) {
    try {
      Dao dao = getDaoForEntity(entity);

      if(dao != null) {
        return dao.delete(entity);
      }
    }
    catch(Exception e) { log.error("Could not delete entity " + entity, e); }

    return false;
  }

  @Override
  public <T extends BaseEntity> T getEntityById(Class<T> type, String id) {
    try {
      Dao dao = getDaoForClass(type);

      if(dao != null) {
        return (T)dao.retrieve(id);
      }
    }
    catch(Exception e) { log.error("Could not get entity of type " + type + " for id " + id, e); }

    return null;
  }

  @Override
  public <T extends BaseEntity> List<T> getEntitiesById(Class<T> type, Collection<String> ids, boolean keepOrderingOfIds) {
    List<T> resultEntities = new ArrayList<>();

    for(String id : ids) {
      resultEntities.add((T)getEntityById(type, id));
    }

    return resultEntities;
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type) {
    try {
      Dao dao = getDaoForClass(type);

      if(dao != null) {
        return dao.retrieveAllEntitiesOfType(type);
      }
    }
    catch(Exception e) { log.error("Could not get all entities of type " + type, e); }

    return new ArrayList<T>();
  }

  @Override
  public void close() {
    manager.close();
  }


  protected Dao getDaoForEntity(Object entity) {
    if(entity == null) {
      log.error("Caught that a Database operation was tried to perform on a null Entity Object");
      return null;
    }

    return getDaoForClass(entity.getClass());
  }

  public Dao getDaoForClass(Class entityClass) {
    Dao dao = mapEntityClassesToDaos.get(entityClass);
    if(dao == null) {
      log.error("It was requested to persist or update Entity of type \" + entityClass + \", but mapEntityClassesToDaos does not contain an Entry for this Entity.");
    }

    return dao;
  }


  public boolean isCouchbaseLiteSystemProperty(String propertyName) {
    return "_id".equals(propertyName) || "_rev".equals(propertyName) || "_revisions".equals(propertyName) ||
        "attachments".equals(propertyName) || "_deleted".equals(propertyName);
  }


  public Database getDatabase() {
    return database;
  }

  public ObjectCache getObjectCache() {
    return objectCache;
  }

}
