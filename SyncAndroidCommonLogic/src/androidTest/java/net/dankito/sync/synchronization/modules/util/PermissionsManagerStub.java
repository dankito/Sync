package net.dankito.sync.synchronization.modules.util;

import android.support.annotation.NonNull;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.PermissionRequestCallback;


public class PermissionsManagerStub implements IPermissionsManager {

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

  }

  @Override
  public void checkPermission(String permission, int rationaleToShowToUserResourceId, PermissionRequestCallback callback) {
    checkPermission(permission, null, callback);
  }

  @Override
  public void checkPermission(String permission, String rationaleToShowToUser, PermissionRequestCallback callback) {
    callback.permissionCheckDone(permission, true);
  }

  @Override
  public boolean isPermissionGranted(String permission) {
    return true;
  }

  @Override
  public void requestPermission(String permission, String rationaleToShowToUser, PermissionRequestCallback callback) {
    callback.permissionCheckDone(permission, true);
  }

}
