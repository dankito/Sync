package net.dankito.sync.javafx.services;

import net.dankito.sync.Device;
import net.dankito.sync.OsType;

public class IconManager {

  public final static String AndroidIconPath = "icons/os/android_icon.png";
  public final static String AndroidLogoPath = "icons/os/android_logo.png";

  public final static String LinuxIconPath = "icons/os/linux_icon.png";
  public final static String LinuxLogoPath = "icons/os/linux_logo.png";

  public final static String WindowsIconPath = "icons/os/windows_icon.png";
  public final static String WindowsLogoPath = "icons/os/windows_logo.png";

  public final static String AppleIconPath = "icons/os/apple_icon.png";
  public final static String AppleLogoPath = "icons/os/apple_logo.png";

  public final static String SolarisIconPath = "icons/os/sun-solaris_icon.png";
  public final static String SolarisLogoPath = "icons/os/sun-solaris_logo.png";

  public final static String ThunderbirdIconPath = "icons/os/thunderbird_icon.png";
  public final static String ThunderbirdLogoPath = "icons/os/thunderbird_logo.png";


  protected static IconManager instance = null;

  public static IconManager getInstance() {
    if(instance == null)
      instance = new IconManager();
    return instance;
  }



  public String getLogoForOperatingSystem(Device device) {
    String platform = device.getOsName();

    if(platform.toLowerCase().contains("android"))
      return AndroidLogoPath;
    else if(device.getOsType() == OsType.THUNDERBIRD)
      return ThunderbirdLogoPath;
    else if(platform.toLowerCase().contains("linux"))
      return LinuxLogoPath;
    else if(platform.toLowerCase().contains("windows"))
      return WindowsLogoPath;
    else if(platform.toLowerCase().contains("mac"))
      return AppleLogoPath;
    else if(platform.toLowerCase().contains("solaris"))
      return SolarisLogoPath;

    return null; // TODO: create a placeholder logo
  }

  public String getIconForOperatingSystem(Device device) {
    String platform = device.getOsName();

    if(platform.toLowerCase().contains("android"))
      return AndroidIconPath;
    else if(device.getOsType() == OsType.THUNDERBIRD)
      return ThunderbirdIconPath;
    else if(platform.toLowerCase().contains("linux"))
      return LinuxIconPath;
    else if(platform.toLowerCase().contains("windows"))
      return WindowsIconPath;
    else if(platform.toLowerCase().contains("mac"))
      return AppleIconPath;
    else if(platform.toLowerCase().contains("solaris"))
      return SolarisIconPath;

    return null; // TODO: create a placeholder icon
  }

}
