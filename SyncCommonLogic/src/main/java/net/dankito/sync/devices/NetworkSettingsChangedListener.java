package net.dankito.sync.devices;


public interface NetworkSettingsChangedListener {

  void settingsChanged(INetworkSettings networkSettings, NetworkSetting setting, Object newValue, Object oldValue);

}
