package net.dankito.sync.data;

import net.dankito.sync.Device;
import net.dankito.sync.LocalConfig;


public interface IDataManager {

  LocalConfig getLocalConfig();

  Device getLocalDevice();

}
