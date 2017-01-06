package net.dankito.devicediscovery;

import net.dankito.utils.IThreadPool;
import net.dankito.utils.ThreadPool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
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


  @Before
  public void setUp() {
    IThreadPool threadPool = new ThreadPool();

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

      }
    });

    try { countDownLatch.await(3, TimeUnit.MINUTES); } catch(Exception ignored) { }

    Assert.assertEquals(1, foundDevicesForFirstDevice.size());
    Assert.assertEquals(SECOND_DISCOVERER_ID, foundDevicesForFirstDevice.get(0));

    Assert.assertEquals(1, foundDevicesForSecondDevice.size());
    Assert.assertEquals(FIRST_DISCOVERER_ID, foundDevicesForSecondDevice.get(0));
  }


  protected void startFirstDiscoverer(IDevicesDiscovererListener listener) {
    DevicesDiscovererConfig config = new DevicesDiscovererConfig(FIRST_DISCOVERER_ID, DISCOVERY_PORT, CHECK_FOR_DEVICES_INTERVAL, listener);

    firstDiscoverer.startAsync(config);
  }

  protected void startSecondDiscoverer(IDevicesDiscovererListener listener) {
    DevicesDiscovererConfig config = new DevicesDiscovererConfig(SECOND_DISCOVERER_ID, DISCOVERY_PORT, CHECK_FOR_DEVICES_INTERVAL, listener);

    secondDiscoverer.startAsync(config);
  }

}
