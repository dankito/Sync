package net.dankito.sync.synchronization;


import net.dankito.sync.BaseEntity;

public interface SynchronizationListener {

  void entitySynchronized(BaseEntity entity);

}
