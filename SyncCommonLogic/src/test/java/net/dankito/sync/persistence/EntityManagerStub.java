package net.dankito.sync.persistence;


import net.dankito.sync.BaseEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * As for Android Instrumentation Tests i can't use Mockito, as i'm exceeding the 65K method limit
 */
public class EntityManagerStub implements IEntityManager {

  protected Map<Class, Map<String, BaseEntity>> persistedEntities = new ConcurrentHashMap<>();

  protected final Field idField;


  public EntityManagerStub() throws NoSuchFieldException {
    idField = BaseEntity.class.getDeclaredField("id");
    idField.setAccessible(true);
  }


  @Override
  public String getDatabasePath() {
    return null;
  }

  @Override
  public boolean persistEntity(BaseEntity entity) {
    try { idField.set(entity, UUID.randomUUID().toString()); } catch(Exception ignored) { } // idField is made accessible in constructor

    Class entityClass = getEntityClass(entity);

    if(persistedEntities.containsKey(entityClass) == false) {
      persistedEntities.put(entityClass, new ConcurrentHashMap<String, BaseEntity>());
    }

    persistedEntities.get(entityClass).put(entity.getId(), entity);

    return true;
  }

  @Override
  public boolean updateEntity(BaseEntity entity) {
    Class entityClass = getEntityClass(entity);

    if(persistedEntities.containsKey(entityClass)) {
      persistedEntities.get(entityClass).put(entity.getId(), entity);
      return true;
    }

    return false;
  }

  @Override
  public boolean updateEntities(List<BaseEntity> entities) {
    for(BaseEntity entity : entities) {
      updateEntity(entity);
    }

    return true;
  }

  @Override
  public boolean deleteEntity(BaseEntity entity) {
    Class entityClass = getEntityClass(entity);

    if(persistedEntities.containsKey(entityClass)) {
      return persistedEntities.remove(entity.getId()) != null;
    }

    return false;
  }

  @Override
  public <T extends BaseEntity> T getEntityById(Class<T> type, String id) {
    if(persistedEntities.containsKey(type)) {
      return (T) persistedEntities.get(type).get(id);
    }

    return null;
  }

  @Override
  public <T extends BaseEntity> List<T> getEntitiesById(Class<T> type, Collection<String> ids, boolean keepOrderingOfIds) {
    return null; // TODO
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type) {
    List<T> entitiesOfType = new ArrayList<>();

    Map<String, BaseEntity> entities = persistedEntities.get(type);
    if(entities != null) {
      entitiesOfType.addAll((Collection<T>)entities.values());
    }

    return entitiesOfType;
  }

  @Override
  public void close() {

  }


  protected Class<? extends BaseEntity> getEntityClass(BaseEntity entity) {
    return entity.getClass();
  }

}
