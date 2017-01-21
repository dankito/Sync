package net.dankito.sync.synchronization.modules;


import net.dankito.sync.localization.Localization;
import net.dankito.sync.synchronization.files.FileSyncService;
import net.dankito.utils.services.IFileStorageService;

public class AndroidPhotosJavaEndpointFileSyncModule extends FileSyncModule {


  public AndroidPhotosJavaEndpointFileSyncModule(Localization localization, FileSyncService fileSyncService, IFileStorageService fileStorageService) {
    super(localization, fileSyncService, fileStorageService);
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
    return SyncModuleDefaultTypes.ANDROID_EXTERNAL_PHOTOS.getTypeName();
  }

}
