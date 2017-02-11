package net.dankito.sync.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.dankito.sync.MainActivity;
import net.dankito.sync.communication.callback.IsSynchronizationPermittedHandler;
import net.dankito.sync.communication.callback.ShouldPermitSynchronizingWithDeviceCallback;
import net.dankito.sync.communication.message.DeviceInfo;


public class AndroidIsSynchronizationPermittedHandler implements IsSynchronizationPermittedHandler {

  public static final String IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME = "IsSynchronizationPermittedHandler";

  public static final String SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION = "ShouldPermitSynchronizingWithDevice";

  public static final String SHOW_CORRECT_RESPONSE_TO_USER_NON_BLOCKING_ACTION = "ShowCorrectResponseToUserNonBlocking";

  public static final String DEVICE_INFO_EXTRA_NAME = "DeviceInfo";

  public static final String CORRECT_RESPONSE_EXTRA_NAME = "CorrectResponse";

  public static final String PERMITS_SYNCHRONIZATION_EXTRA_NAME = "PermitsSynchronization";


  protected Context context;


  public AndroidIsSynchronizationPermittedHandler(Context context) {
    this.context = context;
  }


  @Override
  public void shouldPermitSynchronizingWithDevice(final DeviceInfo remoteDeviceInfo, final ShouldPermitSynchronizingWithDeviceCallback callback) {
    createBroadcastReceiverForShouldPermitSynchronizingWithDeviceResultIntent(remoteDeviceInfo, callback);

    Intent callMainActivityIntent = new Intent(context, MainActivity.class);
    callMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    callMainActivityIntent.putExtra(IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME, SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION);

    callMainActivityIntent.putExtra(DEVICE_INFO_EXTRA_NAME, remoteDeviceInfo.toString());

    context.startActivity(callMainActivityIntent);
  }

  protected void createBroadcastReceiverForShouldPermitSynchronizingWithDeviceResultIntent(final DeviceInfo remoteDeviceInfo, final ShouldPermitSynchronizingWithDeviceCallback callback) {
    IntentFilter filter = new IntentFilter();
    filter.addAction(SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION);

    BroadcastReceiver receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        context.unregisterReceiver(this);
        handleShouldPermitSynchronizingWithDeviceResultIntent(remoteDeviceInfo, intent, callback);
      }
    };

    context.registerReceiver(receiver, filter);
  }

  protected void handleShouldPermitSynchronizingWithDeviceResultIntent(DeviceInfo remoteDeviceInfo, Intent intent, ShouldPermitSynchronizingWithDeviceCallback callback) {
    boolean permitsSynchronization = intent.getBooleanExtra(PERMITS_SYNCHRONIZATION_EXTRA_NAME, false);

    callback.done(remoteDeviceInfo, permitsSynchronization);
  }


  @Override
  public void showCorrectResponseToUserNonBlocking(DeviceInfo remoteDeviceInfo, String correctResponse) {
    Intent callMainActivityIntent = new Intent(context, MainActivity.class);
    callMainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    callMainActivityIntent.putExtra(IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME, SHOW_CORRECT_RESPONSE_TO_USER_NON_BLOCKING_ACTION);

    callMainActivityIntent.putExtra(DEVICE_INFO_EXTRA_NAME, remoteDeviceInfo.toString());
    callMainActivityIntent.putExtra(CORRECT_RESPONSE_EXTRA_NAME, correctResponse);

    context.startActivity(callMainActivityIntent);
  }

}
