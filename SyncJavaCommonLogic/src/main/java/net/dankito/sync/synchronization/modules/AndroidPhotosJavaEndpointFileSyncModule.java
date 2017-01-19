package net.dankito.sync.synchronization.modules;


import net.dankito.sync.localization.Localization;
import net.dankito.utils.services.IFileStorageService;

public class AndroidPhotosJavaEndpointFileSyncModule extends FileSyncModule {


  public AndroidPhotosJavaEndpointFileSyncModule(Localization localization, IFileStorageService fileStorageService) {
    super(localization, fileStorageService);
  }


  @Override
  protected String getNameStringResourceKey() {
    return "sync.module.name.android_photos.java_endpoint";
  }

  @Override
  public int getDisplayPriority() {
    return DISPLAY_PRIORITY_LOW;
  }

  @Override
  public String getSyncEntityTypeItCanHandle() {
    return SyncModuleDefaultTypes.AndroidExternalPhotos.getTypeName();
  }

}
