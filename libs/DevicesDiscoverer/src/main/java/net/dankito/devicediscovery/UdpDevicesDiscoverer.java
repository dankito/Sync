package net.dankito.devicediscovery;

import net.dankito.utils.AsyncProducerConsumerQueue;
import net.dankito.utils.ConsumerListener;
import net.dankito.utils.IThreadPool;
import net.dankito.utils.services.NetworkHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Named;


@Named
public class UdpDevicesDiscoverer implements IDevicesDiscoverer {

  protected static final String MESSAGE_HEADER_AND_BODY_SEPARATOR = " : ";

  protected static final Charset MESSAGES_CHARSET = Charset.forName("utf8");

  protected static final int DELAY_BEFORE_RESTARTING_BROADCAST_FOR_ADDRESS_MILLIS = 5000;

  private static final Logger log = LoggerFactory.getLogger(UdpDevicesDiscoverer.class);


  protected ConnectionsAliveWatcher connectionsAliveWatcher = null;

  protected NetworkHelper networkHelper = null;

  protected IThreadPool threadPool;

  protected Thread listenerThread = null;

  protected DatagramSocket listenerSocket = null;
  protected boolean isListenerSocketOpened = false;

  protected Map<String, Thread> broadcastThreads = new ConcurrentHashMap<>();

  protected List<DatagramSocket> openedBroadcastSockets = new ArrayList<>();
  protected boolean areBroadcastSocketsOpened = false;

  protected Timer timerToRestartBroadcastForBroadcastAddress = null;

  protected AsyncProducerConsumerQueue<ReceivedUdpDevicesDiscovererPacket> receivedPacketsQueue;

  protected List<String> foundDevices = new CopyOnWriteArrayList<>();


  public UdpDevicesDiscoverer(IThreadPool threadPool) {
    this.threadPool = threadPool;

    this.networkHelper = new NetworkHelper();
    receivedPacketsQueue = new AsyncProducerConsumerQueue(3, receivedPacketsHandler);
  }


  @Override
  public boolean isRunning() {
    return isListenerSocketOpened && areBroadcastSocketsOpened;
  }

  @Override
  public void startAsync(DevicesDiscovererConfig config) {
    log.info("Starting UdpDevicesDiscoverer " + config.getLocalDeviceInfo() + " ...");

    // * 3.5 = from 3 messages one must be received to be still valued as 'connected'
    this.connectionsAliveWatcher = new ConnectionsAliveWatcher((int)(config.getCheckForDevicesIntervalMillis() * 10.5));

    startListenerAsync(config);

    startBroadcastAsync(config);
  }

  @Override
  public void stop() {
    log.info("Stopping UdpDevicesDiscoverer ...");

    receivedPacketsQueue.stop();

    if(connectionsAliveWatcher != null) {
      connectionsAliveWatcher.stopWatching();
    }

    stopListener();

    stopBroadcast();
  }

  protected void stopBroadcast() {
    synchronized(broadcastThreads) {
      areBroadcastSocketsOpened = false;

      for(DatagramSocket clientSocket : openedBroadcastSockets) {
        clientSocket.close();
      }

      openedBroadcastSockets.clear();

      for(String broadcastAddress : new ArrayList<>(broadcastThreads.keySet())) {
        Thread broadcastThread = broadcastThreads.get(broadcastAddress);
        try { broadcastThread.join(100); } catch(Exception ignored) { }

        broadcastThreads.remove(broadcastAddress);
        log.info("Stopped broadcasting for Address " + broadcastAddress);
      }
    }
  }

  @Override
  public void disconnectedFromDevice(String deviceInfo) {
    removeDeviceFromFoundDevices(deviceInfo);
  }


  protected void startListenerAsync(final DevicesDiscovererConfig config) {
    stopListener();

    listenerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        startListener(config);
      }
    }, "UdpDevicesDiscoverer_Listener");

    listenerThread.start();
  }

  protected void stopListener() {
    if(listenerThread != null) {
      try { listenerThread.join(100); } catch(Exception ignored) { }

      listenerThread = null;
    }

    if(isListenerSocketOpened) {
      listenerSocket.close();
      listenerSocket = null;
      isListenerSocketOpened = false;
    }
  }

  protected void startListener(DevicesDiscovererConfig config) {
    try {
      this.listenerSocket = createListenerSocket(config.getDiscoverDevicesPort());

      byte[] buffer = new byte[1024];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      while(isListenerSocketOpened) {
        try {
          listenerSocket.receive(packet);
        } catch(Exception ex) {
          if(isSocketCloseException(ex) == true) // communication has been cancelled by close() method
            break;
          else {
            log.error("An Error occurred receiving Packets. listenerSocket = " + listenerSocket, ex);
            startListener(config);
          }
        }

        listenerReceivedPacket(buffer, packet, config);
      }
    } catch(Exception ex) {
      log.error("An error occurred starting UdpDevicesSearcher", ex);
    }
  }

  protected DatagramSocket createListenerSocket(int discoverDevicesPort) throws SocketException {
    DatagramSocket listenerSocket = new DatagramSocket(null); // so that other Applications on the same Host can also use this port, set bindAddress to null ..,
    listenerSocket.setReuseAddress(true); // and reuseAddress to true
    listenerSocket.bind(new InetSocketAddress(discoverDevicesPort));

    listenerSocket.setBroadcast(true);
    isListenerSocketOpened = true;

    return listenerSocket;
  }

  protected boolean isSocketCloseException(Exception ex) {
    return networkHelper.isSocketCloseException(ex);
  }


  protected void listenerReceivedPacket(byte[] buffer, DatagramPacket packet, DevicesDiscovererConfig config) {
    receivedPacketsQueue.add(new ReceivedUdpDevicesDiscovererPacket(Arrays.copyOf(buffer, packet.getLength()), packet, packet.getAddress().getHostAddress(),
        config.getLocalDeviceInfo(), config.getDiscoveryMessagePrefix(), config.getListener()));
  }


  protected ConsumerListener<ReceivedUdpDevicesDiscovererPacket> receivedPacketsHandler = new ConsumerListener<ReceivedUdpDevicesDiscovererPacket>() {
    @Override
    public void consumeItem(ReceivedUdpDevicesDiscovererPacket receivedPacket) {
      handleReceivedPacket(receivedPacket.getReceivedData(), receivedPacket.getPacket(), receivedPacket.getSenderAddress(), receivedPacket.getLocalDeviceInfo(),
          receivedPacket.getDiscoveryMessagePrefix(), receivedPacket.getListener());
    }
  };

  protected void handleReceivedPacket(byte[] receivedData, DatagramPacket packet, String senderAddress, String localDeviceInfo, String discoveryMessagePrefix, DevicesDiscovererListener listener) {
    if(isSearchingForDevicesMessage(receivedData, receivedData.length, discoveryMessagePrefix)) {
      String remoteDeviceInfo = getDeviceInfoFromMessage(receivedData, senderAddress);

      if(isSelfSentPacket(remoteDeviceInfo, localDeviceInfo) == false) {
        if(hasDeviceAlreadyBeenFound(remoteDeviceInfo) == false) {
          deviceFound(remoteDeviceInfo, senderAddress, listener);
        }
        else {
          connectionsAliveWatcher.receivedMessageFromDevice(remoteDeviceInfo);
        }
      }
    }
  }

  protected boolean isSearchingForDevicesMessage(byte[] receivedData, int packetLength, String discoveryMessagePrefix) {
    String receivedMessage = parseBytesToString(receivedData, packetLength);
    return receivedMessage.startsWith(discoveryMessagePrefix);
  }

  protected boolean isSelfSentPacket(String remoteDeviceInfo, String localDeviceInfo) {
    return localDeviceInfo.equals(remoteDeviceInfo);
  }

  protected boolean hasDeviceAlreadyBeenFound(String deviceInfo) {
    List<String> foundDevicesCopy = new ArrayList<>(foundDevices);

    for(String foundDevice : foundDevicesCopy) {
      if(foundDevice.equals(deviceInfo)) {
        return true;
      }
    }

    return false;
  }

  protected void deviceFound(String remoteDeviceInfo, String remoteDeviceAddress, DevicesDiscovererListener listener) {
    log.info("Found Device " + remoteDeviceInfo + " on " + remoteDeviceAddress);

    synchronized(this) {
      foundDevices.add(remoteDeviceInfo);

      if(foundDevices.size() == 1) {
        startConnectionsAliveWatcher(listener);
      }
    }

    listener.deviceFound(remoteDeviceInfo, remoteDeviceAddress);
  }

  protected void startConnectionsAliveWatcher(final DevicesDiscovererListener listener) {
    connectionsAliveWatcher.startWatchingAsync(foundDevices, new ConnectionsAliveWatcherListener() {
      @Override
      public void deviceDisconnected(String deviceInfo) {
        UdpDevicesDiscoverer.this.deviceDisconnected(deviceInfo, listener);
      }
    });
  }

  protected void deviceDisconnected(String deviceInfo, DevicesDiscovererListener listener) {
    removeDeviceFromFoundDevices(deviceInfo);

    if(listener != null) {
      listener.deviceDisconnected(deviceInfo);
    }
  }


  protected void startBroadcastAsync(final DevicesDiscovererConfig config) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        startBroadcast(config);
      }
    });
  }

  protected void startBroadcast(DevicesDiscovererConfig config) {
    for(InetAddress broadcastAddress : networkHelper.getBroadcastAddresses()) {
      startBroadcastForBroadcastAddressAsync(broadcastAddress, config);
    }
  }

  protected void startBroadcastForBroadcastAddressAsync(final InetAddress broadcastAddress, final DevicesDiscovererConfig config) {
    synchronized(broadcastThreads) {
      Thread broadcastThread = new Thread(new Runnable() {
        @Override
        public void run() {
          startBroadcastForBroadcastAddress(broadcastAddress, config);
        }
      }, "UdpDevicesDiscoverer_BroadcastTo_" + broadcastAddress.getHostAddress());

      broadcastThreads.put(broadcastAddress.getHostAddress(), broadcastThread);

      broadcastThread.start();
    }
  }

  protected void startBroadcastForBroadcastAddress(InetAddress broadcastAddress, DevicesDiscovererConfig config) {
    try {
      DatagramSocket broadcastSocket = new DatagramSocket();

      synchronized(broadcastThreads) {
        openedBroadcastSockets.add(broadcastSocket);
        areBroadcastSocketsOpened = true;
      }

      broadcastSocket.setSoTimeout(10000);

      while(broadcastSocket.isClosed() == false) {
        try {
          sendBroadcastOnSocket(broadcastSocket, broadcastAddress, config);
        } catch(Exception e) {
          log.error("Could not send Broadcast to Address " + broadcastAddress, e);

          synchronized(broadcastThreads) {
            openedBroadcastSockets.remove(broadcastSocket);
          }
          broadcastSocket.close();

          restartBroadcastForBroadcastAddress(broadcastAddress, config);

          break;
        }
      }
    } catch (Exception ex) {
      log.error("An error occurred trying to find Devices", ex);
    }
  }

  protected void sendBroadcastOnSocket(DatagramSocket broadcastSocket, InetAddress broadcastAddress, DevicesDiscovererConfig config) throws IOException {
    DatagramPacket searchDevicesPacket = createSearchDevicesDatagramPacket(broadcastAddress, config);
    broadcastSocket.send(searchDevicesPacket);

    try { Thread.sleep(config.getCheckForDevicesIntervalMillis()); } catch(Exception ignored) { }
  }

  protected void restartBroadcastForBroadcastAddress(final InetAddress broadcastAddress, final DevicesDiscovererConfig config) {
    if(timerToRestartBroadcastForBroadcastAddress == null) {
      timerToRestartBroadcastForBroadcastAddress = new Timer(true);
    }

    // TODO: a problem about using Timer is, that then broadcasts are send on Timer thread and not on broadcastThread
    timerToRestartBroadcastForBroadcastAddress.schedule(new TimerTask() {
      @Override
      public void run() {
        startBroadcastForBroadcastAddress(broadcastAddress, config);
      }
    }, DELAY_BEFORE_RESTARTING_BROADCAST_FOR_ADDRESS_MILLIS);
  }

  protected DatagramPacket createSearchDevicesDatagramPacket(InetAddress broadcastAddress, DevicesDiscovererConfig config) {
    String message = config.getDiscoveryMessagePrefix() + MESSAGE_HEADER_AND_BODY_SEPARATOR + config.getLocalDeviceInfo();
    byte[] messageBytes = message.getBytes(MESSAGES_CHARSET);

    return new DatagramPacket(messageBytes, messageBytes.length, broadcastAddress, config.getDiscoverDevicesPort());
  }

  protected String getDeviceInfoFromMessage(byte[] receivedBytes, String senderAddress) {
    String receivedMessage = parseBytesToString(receivedBytes, receivedBytes.length);

    int bodyStartIndex = receivedMessage.indexOf(MESSAGE_HEADER_AND_BODY_SEPARATOR) + MESSAGE_HEADER_AND_BODY_SEPARATOR.length();

    return receivedMessage.substring(bodyStartIndex);
  }

  protected String parseBytesToString(byte[] receivedData, int packetLength) {
    return new String(receivedData, 0, packetLength, MESSAGES_CHARSET);
  }


  protected void removeDeviceFromFoundDevices(String deviceInfo) {
    foundDevices.remove(deviceInfo);
  }

}
