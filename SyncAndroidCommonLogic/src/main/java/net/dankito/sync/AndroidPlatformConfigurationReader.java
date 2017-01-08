package net.dankito.sync;

import android.os.Build;


import net.dankito.sync.data.IPlatformConfigurationReader;

public class AndroidPlatformConfigurationReader implements IPlatformConfigurationReader {

  @Override
  public String getDeviceName() {
    String manufacturer = android.os.Build.MANUFACTURER;
    if(manufacturer.length() > 0 && Character.isLowerCase(manufacturer.charAt(0))) {
      manufacturer = Character.toUpperCase(manufacturer.charAt(0)) + manufacturer.substring(1);
    }

    return manufacturer + " " + android.os.Build.MODEL;
  }

  @Override
  public String getPlatformName() {
    return "Android";
  }

  @Override
  public String getOsVersion() {
    return Build.VERSION.RELEASE;
  }

}
