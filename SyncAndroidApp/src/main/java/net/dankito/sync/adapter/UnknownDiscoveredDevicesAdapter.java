package net.dankito.sync.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import net.dankito.sync.synchronization.modules.AndroidPhotosSyncModule;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.R;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;

import java.util.ArrayList;
import java.util.List;


public class UnknownDiscoveredDevicesAdapter extends DiscoveredDevicesAdapterBase {


  protected ISyncConfigurationManager syncConfigurationManager;


  public UnknownDiscoveredDevicesAdapter(Activity context, IDevicesManager devicesManager, ISyncConfigurationManager syncConfigurationManager) {
    super(context, devicesManager);

    this.syncConfigurationManager = syncConfigurationManager;
  }


  protected List<DiscoveredDevice> getDiscoveredDevicesToShow(IDevicesManager devicesManager) {
    return devicesManager.getUnknownDiscoveredDevices();
  }


  protected int getListItemLayoutId() {
    return R.layout.list_item_unknown_discovered_devices;
  }

  protected void setButtons(View convertView, DiscoveredDevice device) {
    Button btnIgnoreDevice = (Button)convertView.findViewById(R.id.btnIgnoreDevice);
    btnIgnoreDevice.setTag(device);
    btnIgnoreDevice.setOnClickListener(btnIgnoreDeviceClickListener);

    Button btnSynchronizeWithDevice = (Button)convertView.findViewById(R.id.btnSynchronizeWithDevice);
    btnSynchronizeWithDevice.setTag(device);
    btnSynchronizeWithDevice.setOnClickListener(btnSynchronizeWithDeviceClickListener);
  }


  protected View.OnClickListener btnIgnoreDeviceClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      DiscoveredDevice device = (DiscoveredDevice)view.getTag();
      if(device != null) {
        devicesManager.addDeviceToIgnoreList(device);
      }
    }
  };


  protected View.OnClickListener btnSynchronizeWithDeviceClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      DiscoveredDevice device = (DiscoveredDevice)view.getTag();
      if(device != null) {
        // TODO: show ConfigureSyncConfigurationActivity and set SyncModuleConfiguration there, remove ISyncConfigurationManager again
        List<SyncModuleConfiguration> syncModuleConfigurations = new ArrayList<SyncModuleConfiguration>();
        for(ISyncModule syncModule : syncConfigurationManager.getAvailableSyncModules()) {
          for(String syncEntityType : syncModule.getSyncEntityTypesItCanHandle()) {
            syncModuleConfigurations.add(new SyncModuleConfiguration(syncEntityType, syncModule instanceof AndroidPhotosSyncModule));
          }
        }

        devicesManager.startSynchronizingWithDevice(device, syncModuleConfigurations);
      }
    }
  };

}
