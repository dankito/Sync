package net.dankito.sync.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.sync.Device;
import net.dankito.sync.R;
import net.dankito.sync.activities.SynchronizationSettingsActivity;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;

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
    discoveredDevicesChanged();
  }


  protected abstract List<DiscoveredDevice> getDiscoveredDevicesToShow(IDevicesManager devicesManager);

  protected abstract int getListItemLayoutId();

  protected abstract void setButtons(View convertView, DiscoveredDevice device);


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

    setButtons(convertView, device);

    convertView.setTag(device);

    return convertView;
  }

  protected void showDeviceDetails(View convertView, DiscoveredDevice device) {
    ImageView imgvwOsIcon = (ImageView)convertView.findViewById(R.id.imgvwOsIcon);
    imgvwOsIcon.setImageResource(getOsLogoId(device.getDevice()));

    TextView txtvwOsName = (TextView)convertView.findViewById(R.id.txtvwOsName);
    txtvwOsName.setText(device.getDevice().getDeviceFullDisplayName());

    TextView txtvwIpAddress = (TextView)convertView.findViewById(R.id.txtvwIpAddress);
    txtvwIpAddress.setText(device.getAddress());
  }


  protected int getOsLogoId(Device device) {
    String osName = device.getOsName().toLowerCase();

    if(osName.contains("android"))
      return R.drawable.android_logo;
    else if(osName.contains("linux"))
      return R.drawable.linux_logo;
    else if(osName.contains("windows"))
      return R.drawable.windows_logo;
    else if(osName.contains("mac"))
      return R.drawable.apple_logo;
    else if(osName.contains("solaris"))
      return R.drawable.sun_solaris_logo;

    return 0; // TODO: create a placeholder logo
  }


  protected void showSynchronizationSettingsActivity(DiscoveredDevice device) {
    Intent intent = new Intent(context, SynchronizationSettingsActivity.class);
    intent.putExtra(SynchronizationSettingsActivity.REMOTE_DEVICE_UNIQUE_ID_EXTRA_NAME, device.getDevice().getUniqueDeviceId());
    context.startActivity(intent);
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
