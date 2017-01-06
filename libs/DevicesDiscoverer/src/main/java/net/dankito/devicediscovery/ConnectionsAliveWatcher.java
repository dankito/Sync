package net.dankito.devicediscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 24/08/15.
 */
public class ConnectionsAliveWatcher {

  private static final Logger log = LoggerFactory.getLogger(ConnectionsAliveWatcher.class);


  protected int connectionTimeout;

  protected Timer connectionsAliveCheckTimer = null;

  protected Map<String, Long> lastMessageReceivedFromDeviceTimestamps = new ConcurrentHashMap<>();



  public ConnectionsAliveWatcher(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }


  public boolean isRunning() {
    return connectionsAliveCheckTimer != null;
  }

  public void startWatchingAsync(final List<String> foundDevices, final ConnectionsAliveWatcherListener listener) {
    synchronized(this) {
      stopWatching();

      log.info("Starting ConnectionsAliveWatcher ...");

      connectionsAliveCheckTimer = new Timer("ConnectionsAliveWatcher Timer");
      connectionsAliveCheckTimer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          checkIfConnectedDevicesStillAreConnected(foundDevices, listener);
        }
      }, connectionTimeout, connectionTimeout);
    }
  }

  public void stopWatching() {
    synchronized(this) {
      if (connectionsAliveCheckTimer != null) {
        log.info("Stopping ConnectionsAliveWatcher ...");

        connectionsAliveCheckTimer.cancel();
        connectionsAliveCheckTimer = null;
      }
    }
  }


  public void receivedMessageFromDevice(String deviceInfo) {
    lastMessageReceivedFromDeviceTimestamps.put(deviceInfo, new Date().getTime());
  }


  protected void checkIfConnectedDevicesStillAreConnected(List<String> foundDevices, final ConnectionsAliveWatcherListener listener) {
    Long now = new Date().getTime();

    for(final String foundDevice : foundDevices) {
      if(hasDeviceExpired(foundDevice, now)) {
        log.info("Device " + foundDevice + " has disconnected, last message received at " +
            new Date(lastMessageReceivedFromDeviceTimestamps.get(foundDevice)) + ", now = " + new Date(now));
        deviceDisconnected(foundDevice, listener);
      }
    }
  }

  protected boolean hasDeviceExpired(String foundDevice, Long now) {
    Long lastMessageReceivedFromDeviceTimestamp = lastMessageReceivedFromDeviceTimestamps.get(foundDevice);

    if(lastMessageReceivedFromDeviceTimestamp != null) {
      return lastMessageReceivedFromDeviceTimestamp < now - connectionTimeout;
    }

    return false;
  }

  protected void deviceDisconnected(String disconnectedDevice, ConnectionsAliveWatcherListener listener) {
    lastMessageReceivedFromDeviceTimestamps.remove(disconnectedDevice);

    if(listener != null) {
      listener.deviceDisconnected(disconnectedDevice);
    }
  }

}
