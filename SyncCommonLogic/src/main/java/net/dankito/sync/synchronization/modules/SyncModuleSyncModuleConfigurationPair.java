package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncModuleConfiguration;

public class SyncModuleSyncModuleConfigurationPair {

  protected ISyncModule syncModule;

  protected SyncModuleConfiguration syncModuleConfiguration;

  protected boolean isEnabled;

  protected boolean originalIsEnabled;


  public SyncModuleSyncModuleConfigurationPair(ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration) {
    this(syncModule, syncModuleConfiguration, true);
  }

  public SyncModuleSyncModuleConfigurationPair(ISyncModule syncModule, SyncModuleConfiguration syncModuleConfiguration, boolean isEnabled) {
    this.syncModule = syncModule;
    this.syncModuleConfiguration = syncModuleConfiguration;
    this.isEnabled = isEnabled;

    this.originalIsEnabled = isEnabled;
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


  public boolean didConfigurationChange() {
    return isEnabled != originalIsEnabled;
  }


  @Override
  public String toString() {
    return "" + getSyncModule();
  }

}
