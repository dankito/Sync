package net.dankito.sync.communication.callback;

import net.dankito.sync.communication.message.DeviceInfo;


public interface IsSynchronizationPermittedHandler {

  void shouldPermitSynchronizingWithDevice(DeviceInfo remoteDeviceInfo, ShouldPermitSynchronizingWithDeviceCallback callback);

  void showCorrectResponseToUserNonBlocking(DeviceInfo remoteDeviceInfo, String correctResponse);

}
