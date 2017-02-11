package net.dankito.sync.synchronization;


import net.dankito.sync.devices.DevicesManagerConfig;

public class SynchronizationConfig {

  public static final int DEFAULT_SYNCHRONIZATION_PORT = DevicesManagerConfig.DEVICES_DISCOVERER_PORT + 2;

  public static final boolean DEFAULT_ALSO_USE_PULL_REPLICATION = false;

}
