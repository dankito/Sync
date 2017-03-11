package net.dankito.sync.synchronization;

import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncEntityState;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.modules.ISyncModule;


public class SyncEntityChange {

  protected ISyncModule syncModule;

  protected boolean hasIncrementalChange = false;

  protected SyncEntity syncEntity;

  protected SyncEntityState state;

  protected DiscoveredDevice sourceDevice; // TODO: this is again a Sonderbehandlung for Thunderbird


  public SyncEntityChange(ISyncModule syncModule, boolean hasIncrementalChange) {
    this.syncModule = syncModule;
    this.hasIncrementalChange = hasIncrementalChange;
  }

  public SyncEntityChange(ISyncModule syncModule, SyncEntity incrementalChange, SyncEntityState state) {
    this.syncModule = syncModule;
    this.syncEntity = incrementalChange;
    this.state = state;

    this.hasIncrementalChange = incrementalChange != null;
  }

  public SyncEntityChange(ISyncModule syncModule, SyncEntity syncEntity, SyncEntityState state, DiscoveredDevice sourceDevice) {
    this(syncModule, syncEntity, state);

    this.sourceDevice = sourceDevice;
  }


  public ISyncModule getSyncModule() {
    return syncModule;
  }

  public boolean hasIncrementalChange() {
    return hasIncrementalChange;
  }

  public SyncEntity getSyncEntity() {
    return syncEntity;
  }

  public SyncEntityState getState() {
    return state;
  }

  public DiscoveredDevice getSourceDevice() {
    return sourceDevice;
  }


  @Override
  public String toString() {
    return getSyncModule() + ": " + getSyncEntity();
  }

}
