package net.dankito.sync.util;

import android.app.Activity;
import android.os.Looper;
import android.support.v7.app.AlertDialog;


public class AlertHelper {

  public static void showInfoMessage(Activity activity, int infoMessageResId) {
    showInfoMessage(activity, activity.getString(infoMessageResId));
  }

  public static void showInfoMessage(Activity activity, int infoMessageResId, int alertTitleResId) {
    showInfoMessage(activity, activity.getString(infoMessageResId), activity.getString(alertTitleResId));
  }

  public static void showInfoMessage(Activity activity, CharSequence infoMessage) {
    showInfoMessage(activity, infoMessage, null);
  }

  public static void showInfoMessage(final Activity activity, final CharSequence infoMessage, final CharSequence alertTitle) {
    if(isRunningOnUiThread() == true) {
      showInfoMessageOnUiThread(activity, infoMessage, alertTitle);
    }
    else {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showInfoMessageOnUiThread(activity, infoMessage, alertTitle);
        }
      });
    }
  }

  protected static void showInfoMessageOnUiThread(final Activity activity, final CharSequence infoMessage, final CharSequence alertTitle) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    if(alertTitle != null) {
      builder = builder.setTitle(alertTitle);
    }
    builder = builder.setMessage(infoMessage);

    builder.setNegativeButton(android.R.string.ok, null);

    builder.create().show();
  }


  public static void showErrorMessage(final Activity activity, int errorMessageResId) {
    showErrorMessage(activity, activity.getString(errorMessageResId));
  }

  public static void showErrorMessage(final Activity activity, int errorMessageResId, int alertTitleResId) {
    showErrorMessage(activity, activity.getString(errorMessageResId), activity.getString(alertTitleResId));
  }

  public static void showErrorMessage(final Activity activity, final CharSequence errorMessage) {
    showErrorMessage(activity, errorMessage, null);
  }

  public static void showErrorMessage(final Activity activity, final CharSequence errorMessage, final CharSequence alertTitle) {
    if(isRunningOnUiThread() == true) {
      showErrorMessageOnUiThread(activity, errorMessage, alertTitle);
    }
    else {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          showErrorMessageOnUiThread(activity, errorMessage, alertTitle);
        }
      });
    }
  }

  public static void showErrorMessageOnUiThread(final Activity activity, final CharSequence errorMessage, final CharSequence alertTitle) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    if(alertTitle != null) {
      builder = builder.setTitle(alertTitle);
    }
    builder = builder.setMessage(errorMessage);
    // TODO: set error icon

    builder.setNegativeButton(android.R.string.ok, null);

    builder.create().show();
  }



  protected static boolean isRunningOnUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread();
  }

}
