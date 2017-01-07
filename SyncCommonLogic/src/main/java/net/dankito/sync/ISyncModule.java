package net.dankito.sync;

/**
 * Created by ganymed on 05/01/17.
 */

public interface ISyncModule {

  void readAllEntitiesAsync(SyncModuleConfiguration syncModuleConfiguration, ReadEntitiesCallback callback);

}
