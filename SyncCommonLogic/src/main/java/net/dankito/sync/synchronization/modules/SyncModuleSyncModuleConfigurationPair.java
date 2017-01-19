package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncModuleConfiguration;

public class SyncModuleSyncModuleConfigurationPair {

  protected ISyncModule syncModule;

  protected SyncModuleConfiguration syncModuleConfiguration;

  protected boolean isEnabled;

  protected boolean originalIsEnabled;

  protected boolean isBidirectional;

  protected boolean originalIsBidirectional;


  public SyncModuleSyncModuleConfigurationPair(ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration) {
    this(syncModule, syncModuleConfiguration, true);
  }

  public SyncModuleSyncModuleConfigurationPair(ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration, boolean isEnabled) {
    this.syncModule = syncModule;
    this.syncModuleConfiguration = syncModuleConfiguration;
    this.isEnabled = isEnabled;

    this.originalIsEnabled = isEnabled;
    this.isBidirectional = syncModuleConfiguration.isBidirectional();
    this.originalIsBidirectional = syncModuleConfiguration.isBidirectional();
  }

  public ISyncModule getSyncModule() {
    return syncModule;
  }

  public SyncModuleConfiguration getSyncModuleConfiguration() {
    return syncModuleConfiguration;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public boolean isBidirectional() {
    return isBidirectional;
  }

  public void setBidirectional(boolean bidirectional) {
    isBidirectional = bidirectional;
  }


  public boolean didConfigurationChange() {
    return isEnabled != originalIsEnabled || isBidirectional != originalIsBidirectional;
  }


  @Override
  public String toString() {
    return "" + getSyncModule();
  }

}
