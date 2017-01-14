package net.dankito.sync.data;


import javax.inject.Named;


@Named
public class JavaPlatformConfigurationReader implements IPlatformConfigurationReader {

  @Override
  public String getDeviceName() {
    // e.g. Lenovo T530, Dell Inspire, ... Don't know how to do this in Java
    return "";
  }

  @Override
  public String getPlatformName() {
    return System.getProperty("os.name");
  }

  @Override
  public String getOsVersion() {
    return System.getProperty("os.version");
  }

}
