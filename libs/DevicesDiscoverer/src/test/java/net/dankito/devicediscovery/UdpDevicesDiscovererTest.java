package net.dankito.devicediscovery;

import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 06/01/17.
 */

public class UdpDevicesDiscovererTest {

  protected static final int DISCOVERY_PORT = 32788;

  protected static final int CHECK_FOR_DEVICES_INTERVAL = 100;

  protected static final String FIRST_DISCOVERER_ID = "Gandhi";

  protected static final String SECOND_DISCOVERER_ID = "Mandela";


  protected UdpDevicesDiscoverer firstDiscoverer;

  protected UdpDevicesDiscoverer secondDiscoverer;

  protected IThreadPool threadPool;


  @Before
  public void setUp() {
    threadPool = new ThreadPool();

    firstDiscoverer = new UdpDevicesDiscoverer(threadPool);
    secondDiscoverer = new UdpDevicesDiscoverer(threadPool);
  }

  @After
  public void tearDown() {
    firstDiscoverer.stop();
    secondDiscoverer.stop();
  }


  @Test
  public void startTwoInstances_BothGetDiscovered() {
    final CountDownLatch countDownLatch = new CountDownLatch(2);

    final List<String> foundDevicesForFirstDevice = new CopyOnWriteArrayList<>();
    startFirstDiscoverer(new IDevicesDiscovererListener() {
      @Override
      public void deviceFound(String deviceInfo, String address) {
        foundDevicesForFirstDevice.add(deviceInfo);
        countDownLatch.countDown();
      }

      @Override
      public void deviceDisconnected(String deviceInfo) {
        foundDevicesForFirstDevice.remove(deviceInfo);
        countDownLatch.countDown();
      }
    });

    final List<String> foundDevicesForSecondDevice = new CopyOnWriteArrayList<>();
    startSecondDiscoverer(new IDevicesDiscovererListener() {
      @Override
      public void deviceFound(String deviceInfo, String address) {
        foundDevicesForSecondDevice.add(deviceInfo);
        countDownLatch.countDown();
      }

      @Override
      public void deviceDisconnected(String deviceInfo) {
        foundDevicesForSecondDevice.add(deviceInfo);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, foundDevicesForFirstDevice.size());
    Assert.assertEquals(SECOND_DISCOVERER_ID, foundDevicesForFirstDevice.get(0));

    Assert.assertEquals(1, foundDevicesForSecondDevice.size());
    Assert.assertEquals(FIRST_DISCOVERER_ID, foundDevicesForSecondDevice.get(0));
  }

  @Test
  public void startElevenInstances_AllGetDiscovered() {
    final Map<String, List<String>> discoveredDevices = new ConcurrentHashMap<>();
    final List<IDevicesDiscoverer> createdDiscoverers = new CopyOnWriteArrayList<>();

    for(int i = 0; i < 11; i++) {
      IDevicesDiscoverer discoverer = new UdpDevicesDiscoverer(threadPool);
      createdDiscoverers.add(discoverer);

      final String deviceId = "" + (i + 1);
      discoveredDevices.put(deviceId, new CopyOnWriteArrayList<String>());

      startDiscoverer(discoverer, deviceId, new IDevicesDiscovererListener() {
        @Override
        public void deviceFound(String deviceInfo, String address) {
          List<String> discoveredDevicesForDevice = discoveredDevices.get(deviceId);
          discoveredDevicesForDevice.add(deviceInfo);
        }

        @Override
        public void deviceDisconnected(String deviceInfo) {
          List<String> discoveredDevicesForDevice = discoveredDevices.get(deviceId);
          discoveredDevicesForDevice.remove(deviceInfo);
        }
      });
    }

    try { Thread.sleep(3000); } catch(Exception ignored) { }

    for(String deviceId : discoveredDevices.keySet()) {
      List<String> discoveredDevicesForDevice = discoveredDevices.get(deviceId);
      Assert.assertEquals(10, discoveredDevicesForDevice.size());
      Assert.assertFalse(discoveredDevicesForDevice.contains(deviceId));
    }

    for(IDevicesDiscoverer discoverer : createdDiscoverers) {
      discoverer.stop();
    }
  }


  @Test
  public void startTwoInstances_DisconnectOne() {
    final CountDownLatch countDownLatch = new CountDownLatch(3);

    final List<String> foundDevicesForFirstDevice = new CopyOnWriteArrayList<>();
    startFirstDiscoverer(new IDevicesDiscovererListener() {
      @Override
      public void deviceFound(String deviceInfo, String address) {
        foundDevicesForFirstDevice.add(deviceInfo);
        countDownLatch.countDown();
      }

      @Override
      public void deviceDisconnected(String deviceInfo) {
        foundDevicesForFirstDevice.remove(deviceInfo);
        countDownLatch.countDown();
      }
    });

    final List<String> foundDevicesForSecondDevice = new CopyOnWriteArrayList<>();
    startSecondDiscoverer(new IDevicesDiscovererListener() {
      @Override
      public void deviceFound(String deviceInfo, String address) {
        foundDevicesForSecondDevice.add(deviceInfo);
        countDownLatch.countDown();
      }

      @Override
      public void deviceDisconnected(String deviceInfo) {
        foundDevicesForSecondDevice.remove(deviceInfo);
        countDownLatch.countDown();
      }
    });

    try { Thread.sleep(300); } catch(Exception ignored) { }

    firstDiscoverer.stop();

    try { countDownLatch.await(3, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, foundDevicesForFirstDevice.size());
    Assert.assertEquals(SECOND_DISCOVERER_ID, foundDevicesForFirstDevice.get(0));

    Assert.assertEquals(0, foundDevicesForSecondDevice.size());
  }

  @Test
  public void startElevenInstances_FiveGetDisconnected() {
    final Map<IDevicesDiscoverer, List<String>> discoveredDevices = new ConcurrentHashMap<>();
    final List<IDevicesDiscoverer> createdDiscoverers = new CopyOnWriteArrayList<>();

    for(int i = 0; i < 11; i++) {
      final IDevicesDiscoverer discoverer = new UdpDevicesDiscoverer(threadPool);
      createdDiscoverers.add(discoverer);
      discoveredDevices.put(discoverer, new CopyOnWriteArrayList<String>());

      startDiscoverer(discoverer, "" + (i + 1), new IDevicesDiscovererListener() {
        @Override
        public void deviceFound(String deviceInfo, String address) {
          List<String> discoveredDevicesForDevice = discoveredDevices.get(discoverer);
          discoveredDevicesForDevice.add(deviceInfo);
        }

        @Override
        public void deviceDisconnected(String deviceInfo) {
          List<String> discoveredDevicesForDevice = discoveredDevices.get(discoverer);
          if(discoveredDevicesForDevice != null) {
            discoveredDevicesForDevice.remove(deviceInfo);
          }
        }
      });
    }

    try { Thread.sleep(500); } catch(Exception ignored) { }

    for(int i = 0; i < 5; i++) {
      IDevicesDiscoverer discoverer = createdDiscoverers.get(i);
      discoveredDevices.remove(discoverer);

      discoverer.stop();
    }

    try { Thread.sleep(5000); } catch(Exception ignored) { }

    for(IDevicesDiscoverer discoverer : discoveredDevices.keySet()) {
      List<String> discoveredDevicesForDevice = discoveredDevices.get(discoverer);
      Assert.assertEquals(5, discoveredDevicesForDevice.size());
    }

    for(IDevicesDiscoverer discoverer : createdDiscoverers) {
      discoverer.stop();
    }
  }


  protected void startFirstDiscoverer(IDevicesDiscovererListener listener) {
    startDiscoverer(firstDiscoverer, FIRST_DISCOVERER_ID, listener);
  }

  protected void startSecondDiscoverer(IDevicesDiscovererListener listener) {
    startDiscoverer(secondDiscoverer, SECOND_DISCOVERER_ID, listener);
  }

  protected void startDiscoverer(IDevicesDiscoverer discoverer, String deviceId, IDevicesDiscovererListener listener) {
    DevicesDiscovererConfig config = new DevicesDiscovererConfig(deviceId, DISCOVERY_PORT, CHECK_FOR_DEVICES_INTERVAL, listener);

    discoverer.startAsync(config);
  }

}
