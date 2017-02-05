package net.dankito.sync.synchronization.modules;


import net.dankito.sync.SyncEntity;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.localization.Localization;


public abstract class SyncModuleBase implements ISyncModule {

  public static final int DISPLAY_PRIORITY_HIGHEST = 1;

  public static final int DISPLAY_PRIORITY_HIGH = 10;

  public static final int DISPLAY_PRIORITY_MEDIUM = 100;

  public static final int DISPLAY_PRIORITY_LOW = 1000;

  public static final int DISPLAY_PRIORITY_LOWEST = 10000;


  protected Localization localization;


  public SyncModuleBase(Localization localization) {
    this.localization = localization;
  }


  protected abstract String getNameStringResourceKey();


  public String getName() {
    return localization.getLocalizedString(getNameStringResourceKey());
  }


  @Override
  public boolean deleteSyncEntityProperty(SyncEntity entity, SyncEntity property) {
    return false; // true for most SyncModules
  }

  @Override
  public void configureLocalSynchronizationSettings(DiscoveredDevice remoteDevice, SyncModuleConfiguration syncModuleConfiguration) {
    // nothing to do more most SyncModules
  }


  @Override
  public String toString() {
    return getName();
  }

}
