package net.dankito.sync.persistence;


import net.dankito.sync.BaseEntity;

import java.util.Collection;
import java.util.List;

/**
 * As for Android Instrumentation Tests i can't use Mockito, as i'm exceeding the 65K method limit
 */
public class EntityManagerStub implements IEntityManager {

  @Override
  public String getDatabasePath() {
    return null;
  }

  @Override
  public boolean persistEntity(BaseEntity entity) {
    return false;
  }

  @Override
  public boolean updateEntity(BaseEntity entity) {
    return false;
  }

  @Override
  public boolean updateEntities(List<BaseEntity> entities) {
    return false;
  }

  @Override
  public boolean deleteEntity(BaseEntity entity) {
    return false;
  }

  @Override
  public <T extends BaseEntity> T getEntityById(Class<T> type, String id) {
    return null;
  }

  @Override
  public <T extends BaseEntity> List<T> getEntitiesById(Class<T> type, Collection<String> ids, boolean keepOrderingOfIds) {
    return null;
  }

  @Override
  public <T extends BaseEntity> List<T> getAllEntitiesOfType(Class<T> type) {
    return null;
  }

  @Override
  public void close() {

  }
}
