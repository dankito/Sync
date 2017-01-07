package net.dankito.sync;

/**
 * Created by ganymed on 07/01/17.
 */

public class LocalConfig extends BaseEntity {

  protected Device localDevice;


  public LocalConfig(Device localDevice) {
    this.localDevice = localDevice;
  }


  public Device getLocalDevice() {
    return localDevice;
  }


  @Override
  public String toString() {
    return "" + getLocalDevice();
  }

}
