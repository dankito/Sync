package net.dankito.sync.synchronization;

import net.dankito.sync.communication.IRequestHandler;

/**
 * Created by ganymed on 07/10/16.
 */
public class NoOpSyncManager implements ISyncManager {

  @Override
  public void stop() {

  }

  @Override
  public boolean addSynchronizationListener(SynchronizationListener listener) {
    return false;
  }

  @Override
  public boolean removeSynchronizationListener(SynchronizationListener listener) {
    return false;
  }

  @Override
  public IRequestHandler getRequestStartSynchronizationHandler() {
    return null;
  }

}
