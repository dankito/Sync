package net.dankito.sync.synchronization.modules;


import net.dankito.sync.localization.Localization;


public class ContactsJavaEndpointSyncModule extends SyncModuleWithoutSystemStorage {


  public ContactsJavaEndpointSyncModule(Localization localization) {
    super(localization);
  }


  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.contacts";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_HIGH;
  }

  @Override
  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.CONTACTS.getTypeName();
  }

}
