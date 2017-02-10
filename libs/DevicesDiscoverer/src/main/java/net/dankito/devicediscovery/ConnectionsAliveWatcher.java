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

    for(final String foundDeviceKey : foundDevices) {
      if(hasDeviceExpired(foundDeviceKey, now)) {
        log.info("Device " + foundDeviceKey + " has disconnected, last message received at " +
            new Date(lastMessageReceivedFromDeviceTimestamps.get(foundDeviceKey)) + ", now = " + new Date(now));
        deviceDisconnected(foundDeviceKey, listener);
      }
    }
  }

  protected boolean hasDeviceExpired(String foundDeviceKey, Long now) {
    Long lastMessageReceivedFromDeviceTimestamp = lastMessageReceivedFromDeviceTimestamps.get(foundDeviceKey);

    if(lastMessageReceivedFromDeviceTimestamp != null) {
      return lastMessageReceivedFromDeviceTimestamp < now - connectionTimeout;
    }

    return false;
  }

  protected void deviceDisconnected(String disconnectedDeviceKey, ConnectionsAliveWatcherListener listener) {
    lastMessageReceivedFromDeviceTimestamps.remove(disconnectedDeviceKey);

    if(listener != null) {
      listener.deviceDisconnected(disconnectedDeviceKey);
    }
  }

}
