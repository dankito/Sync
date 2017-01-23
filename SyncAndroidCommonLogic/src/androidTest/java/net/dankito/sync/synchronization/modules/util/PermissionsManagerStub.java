package net.dankito.sync.synchronization.modules.util;

import android.support.annotation.NonNull;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.MultiplePermissionsRequestCallback;
import net.dankito.android.util.services.PermissionRequestCallback;

import java.util.HashMap;
import java.util.Map;


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
  public void checkPermissions(String[] permissions, String[] rationalesToShowToUser, MultiplePermissionsRequestCallback callback) {
    Map<String, Boolean> permissionsResult = new HashMap<>();
    for(int i = 0; i < permissions.length; i++) {
      permissionsResult.put(permissions[i], true);
    }

    callback.permissionsCheckDone(permissionsResult);
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
