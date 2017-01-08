package net.dankito.sync.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.sync.R;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.utils.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class DiscoveredDevicesAdapterBase extends BaseAdapter {

  protected Activity context;

  protected IDevicesManager devicesManager;

  protected List<DiscoveredDevice> discoveredDevicesToShow = new CopyOnWriteArrayList<>();


  public DiscoveredDevicesAdapterBase(Activity context, IDevicesManager devicesManager) {
    this.context = context;
    this.devicesManager = devicesManager;

    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);
  }


  protected abstract List<DiscoveredDevice> getDiscoveredDevicesToShow(IDevicesManager devicesManager);

  protected abstract int getListItemLayoutId();

  protected abstract void setButtons(View convertView);


  @Override
  public int getCount() {
    return discoveredDevicesToShow.size();
  }

  @Override
  public Object getItem(int position) {
    return discoveredDevicesToShow.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parentView) {
    if(convertView == null) {
      convertView = context.getLayoutInflater().inflate(getListItemLayoutId(), parentView, false);
    }

    DiscoveredDevice device = (DiscoveredDevice)getItem(position);

    showDeviceDetails(convertView, device);

    setButtons(convertView);

    convertView.setTag(device);

    return convertView;
  }

  protected void showDeviceDetails(View convertView, DiscoveredDevice device) {
    ImageView imgvwOsIcon = (ImageView)convertView.findViewById(R.id.imgvwOsIcon);
    // TODO

    TextView txtvwOsName = (TextView)convertView.findViewById(R.id.txtvwOsName);
    String osName = device.getDevice().getOsName() + " " + device.getDevice().getOsVersion();
    if(StringUtils.isNotNullOrEmpty(device.getDevice().getName())) {
      osName = device.getDevice().getName() + " " + osName;
    }
    txtvwOsName.setText(osName);

    TextView txtvwIpAddress = (TextView)convertView.findViewById(R.id.txtvwIpAddress);
    txtvwIpAddress.setText(device.getAddress());
  }


  protected DiscoveredDevicesListener discoveredDevicesListener = new DiscoveredDevicesListener() {
    @Override
    public void deviceDiscovered(DiscoveredDevice connectedDevice, DiscoveredDeviceType type) {
      discoveredDevicesChangedThreadSafe();
    }

    @Override
    public void disconnectedFromDevice(DiscoveredDevice disconnectedDevice) {
      discoveredDevicesChangedThreadSafe();
    }
  };

  protected void discoveredDevicesChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        discoveredDevicesChanged();
      }
    });
  }

  protected void discoveredDevicesChanged() {
    this.discoveredDevicesToShow = new CopyOnWriteArrayList<>(getDiscoveredDevicesToShow(devicesManager));

    notifyDataSetChanged();
  }

}
