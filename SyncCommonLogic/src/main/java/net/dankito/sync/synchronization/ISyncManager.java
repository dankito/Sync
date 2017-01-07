package net.dankito.sync.synchronization;

/**
 * Created by ganymed on 25/11/14.
 */
public interface ISyncManager {

  boolean addSynchronizationListener(SynchronizationListener listener);

  boolean removeSynchronizationListener(SynchronizationListener listener);

  void stop();

}
