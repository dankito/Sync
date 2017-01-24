package net.dankito.sync.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import net.dankito.sync.R;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;

import java.util.List;


public class UnknownDiscoveredDevicesAdapter extends DiscoveredDevicesAdapterBase {


  public UnknownDiscoveredDevicesAdapter(Activity context, IDevicesManager devicesManager) {
    super(context, devicesManager);
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

      showSynchronizationSettingsActivity(device);
    }
  };

}
