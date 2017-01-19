package net.dankito.sync.synchronization.modules;


import net.dankito.sync.localization.Localization;


public class CallLogJavaEndpointSyncModule extends SyncModuleWithoutSystemStorage {


  public CallLogJavaEndpointSyncModule(Localization localization) {
    super(localization);
  }


  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.call_log";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_HIGHEST;
  }

  @Override
  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.CALL_LOG.getTypeName();
  }

}
