package net.dankito.sync.synchronization.modules;


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

}
