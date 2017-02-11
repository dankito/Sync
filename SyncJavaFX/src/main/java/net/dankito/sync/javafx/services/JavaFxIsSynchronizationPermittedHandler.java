package net.dankito.sync.javafx.services;

import net.dankito.sync.communication.callback.IsSynchronizationPermittedHandler;
import net.dankito.sync.communication.callback.ShouldPermitSynchronizingWithDeviceCallback;
import net.dankito.sync.communication.message.DeviceInfo;
import net.dankito.sync.javafx.AlertHelper;
import net.dankito.sync.javafx.FXUtils;
import net.dankito.sync.localization.Localization;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.application.Platform;


@Named
public class JavaFxIsSynchronizationPermittedHandler implements IsSynchronizationPermittedHandler {

  @Inject
  protected Localization localization;


  @Override
  public void shouldPermitSynchronizingWithDevice(DeviceInfo remoteDeviceInfo, ShouldPermitSynchronizingWithDeviceCallback callback) {
    FXUtils.runOnUiThread(() -> shouldPermitSynchronizingWithDeviceOnUiThread(remoteDeviceInfo, callback));
  }

  protected void shouldPermitSynchronizingWithDeviceOnUiThread(DeviceInfo remoteDeviceInfo, ShouldPermitSynchronizingWithDeviceCallback callback) {
    String message = localization.getLocalizedString("alert.message.permit.device.to.synchronize", remoteDeviceInfo);
    String alertTitle = localization.getLocalizedString("alert.title.permit.device.to.synchronize");

    boolean permitsSynchronization = AlertHelper.showConfirmationDialogOnUiThread(message, alertTitle);

    callback.done(remoteDeviceInfo, permitsSynchronization);
  }

  @Override
  public void showCorrectResponseToUserNonBlocking(DeviceInfo remoteDeviceInfo, String correctResponse) {
    Platform.runLater(() -> showCorrectResponseToUserOnUiThread(remoteDeviceInfo, correctResponse));
  }

  public void showCorrectResponseToUserOnUiThread(DeviceInfo remoteDeviceInfo, String correctResponse) {
    String message = localization.getLocalizedString("alert.message.enter.this.code.on.remote.device", remoteDeviceInfo, correctResponse);

    AlertHelper.showInfoMessage(message, null);
  }

}
