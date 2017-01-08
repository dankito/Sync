package net.dankito.sync.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import net.dankito.sync.R;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;

import java.util.List;


public class KnownSynchronizedDiscoveredDevicesAdapter extends DiscoveredDevicesAdapterBase {


  public KnownSynchronizedDiscoveredDevicesAdapter(Activity context, IDevicesManager devicesManager) {
    super(context, devicesManager);
  }


  protected List<DiscoveredDevice> getDiscoveredDevicesToShow(IDevicesManager devicesManager) {
    return devicesManager.getKnownSynchronizedDiscoveredDevices();
  }


  protected int getListItemLayoutId() {
    return R.layout.list_item_known_synchronized_discovered_devices;
  }

  protected void setButtons(View convertView, DiscoveredDevice device) {
    Button btnStopSynchronizingWithDevice = (Button)convertView.findViewById(R.id.btnStopSynchronizingWithDevice);
    btnStopSynchronizingWithDevice.setTag(device);
    btnStopSynchronizingWithDevice.setOnClickListener(btnStopSynchronizingWithDeviceClickListener);

    Button btnViewSynchronizationLog = (Button)convertView.findViewById(R.id.btnViewSynchronizationLog);
    btnViewSynchronizationLog.setTag(device);
    btnViewSynchronizationLog.setOnClickListener(btnViewSynchronizationLogClickListener);
  }


  protected View.OnClickListener btnStopSynchronizingWithDeviceClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      DiscoveredDevice device = (DiscoveredDevice)view.getTag();
      if(device != null) {
        devicesManager.stopSynchronizingWithDevice(device);
      }
    }
  };


  protected View.OnClickListener btnViewSynchronizationLogClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {

    }
  };

}
