package net.dankito.sync.synchronization;

import net.dankito.sync.communication.IRequestHandler;

/**
 * Created by ganymed on 25/11/14.
 */
public interface ISyncManager {

  void stop();

  boolean addSynchronizationListener(SynchronizationListener listener);

  boolean removeSynchronizationListener(SynchronizationListener listener);

  IRequestHandler getRequestStartSynchronizationHandler();

}
