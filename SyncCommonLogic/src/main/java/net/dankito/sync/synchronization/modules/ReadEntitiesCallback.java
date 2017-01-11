package net.dankito.sync.synchronization.modules;

import net.dankito.sync.SyncEntity;

import java.util.List;

/**
 * Created by ganymed on 05/01/17.
 */

public interface ReadEntitiesCallback {

  void done(List<SyncEntity> entities);

}
