package net.dankito.sync.communication.callback;

import net.dankito.sync.communication.message.DeviceInfo;


public interface ShouldPermitSynchronizingWithDeviceCallback {

  void done(DeviceInfo remoteDeviceInfo, boolean permitsSynchronization);

}
