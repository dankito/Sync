package net.dankito.sync;

import net.dankito.sync.config.DatabaseTableConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;


@Entity(name = DatabaseTableConfig.LOCAL_CONFIG_TABLE_NAME)
public class LocalConfig extends BaseEntity {

  @OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST } )
  @JoinColumn(name = DatabaseTableConfig.LOCAL_CONFIG_LOCAL_DEVICE_ID_JOIN_COLUMN_NAME)
  protected Device localDevice;


  protected LocalConfig() { // for reflection

  }

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
