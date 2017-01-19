package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncModuleConfiguration;

public class SyncModuleSyncModuleConfigurationPair {

  protected ISyncModule syncModule;

  protected SyncModuleConfiguration syncModuleConfiguration;

  protected boolean isEnabled;


  public SyncModuleSyncModuleConfigurationPair(ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration) {
    this.syncModule = syncModule;
    this.syncModuleConfiguration = syncModuleConfiguration;
  }

  public SyncModuleSyncModuleConfigurationPair(ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration, boolean isEnabled) {
    this(syncModule, syncModuleConfiguration);
    this.isEnabled = isEnabled;
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


  @Override
  public String toString() {
    return "" + getSyncModule();
  }

}
