package net.dankito.sync.synchronization;

import net.dankito.sync.SyncEntity;


public interface SyncEntityChangeListener {

  void entityChanged(SyncEntity entity);

}
