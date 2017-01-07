package net.dankito.sync.persistence;


import net.dankito.sync.BaseEntity;

import java.util.Collection;
import java.util.List;


public interface IEntityManager {

  String getDatabasePath();

  boolean persistEntity(BaseEntity entity);

  boolean updateEntity(BaseEntity entity);
  boolean updateEntities(List<BaseEntity> entities);

  boolean deleteEntity(BaseEntity entity);

  <T extends BaseEntity> T getEntityById(Class<T> type, String id);
  <T extends BaseEntity> List<T> getEntitiesById(Class<T> type, Collection<String> ids, boolean keepOrderingOfIds);
  <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type);

  void close();

}
