package net.dankito.sync.synchronization;

/**
 * Created by ganymed on 25/11/14.
 */
public interface ISyncManager {

  void stop();

  boolean addSynchronizationListener(SynchronizationListener listener);

  boolean removeSynchronizationListener(SynchronizationListener listener);

}
