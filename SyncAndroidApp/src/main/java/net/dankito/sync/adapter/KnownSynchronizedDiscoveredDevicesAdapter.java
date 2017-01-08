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

  protected void setButtons(View convertView) {
    Button btnStopSynchronizingWithDevice = (Button)convertView.findViewById(R.id.btnStopSynchronizingWithDevice);
    btnStopSynchronizingWithDevice.setOnClickListener(btnStopSynchronizingWithDeviceClickListener);

    Button btnViewSynchronizationLog = (Button)convertView.findViewById(R.id.btnViewSynchronizationLog);
    btnViewSynchronizationLog.setOnClickListener(btnViewSynchronizationLogClickListener);
  }


  protected View.OnClickListener btnStopSynchronizingWithDeviceClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {

    }
  };


  protected View.OnClickListener btnViewSynchronizationLogClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {

    }
  };

}
