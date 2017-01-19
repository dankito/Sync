package net.dankito.sync.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import net.dankito.sync.R;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncConfigurationWithDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SyncModuleConfigurationsAdapter extends BaseAdapter {

  protected Activity activity;

  protected ISyncModuleConfigurationManager syncModuleConfigurationManager;

  protected SyncConfigurationWithDevice syncModuleConfigurationsForDevice;

  protected List<SyncModuleSyncModuleConfigurationPair> sortedSyncConfigurationModules;


  public SyncModuleConfigurationsAdapter(Activity activity, ISyncModuleConfigurationManager syncModuleConfigurationManager, DiscoveredDevice remoteDevice) {
    this.activity = activity;
    this.syncModuleConfigurationManager = syncModuleConfigurationManager;

    this.syncModuleConfigurationsForDevice = syncModuleConfigurationManager.getSyncModuleConfigurationsForDevice(remoteDevice);
    this.sortedSyncConfigurationModules = new ArrayList<>(syncModuleConfigurationsForDevice.getSyncModuleConfigurations());

    sortSyncConfigurationModules();
  }

  private void sortSyncConfigurationModules() {
    Collections.sort(sortedSyncConfigurationModules, new Comparator<SyncModuleSyncModuleConfigurationPair>() {
      @Override
      public int compare(SyncModuleSyncModuleConfigurationPair o1, SyncModuleSyncModuleConfigurationPair o2) {
        int displayPriority1 = o1.getSyncModule().getDisplayPriority();
        int displayPriority2 = o2.getSyncModule().getDisplayPriority();

        if(displayPriority1 > displayPriority2) {
          return 1;
        }
        if(displayPriority1 == displayPriority2) {
          return 0;
        }
        return -1;
      }
    });
  }


  @Override
  public int getCount() {
    return sortedSyncConfigurationModules.size();
  }

  @Override
  public Object getItem(int position) {
    return sortedSyncConfigurationModules.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parentView) {
    if(convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.list_item_sync_module_configuration, parentView, false);
    }

    SyncModuleSyncModuleConfigurationPair pair = (SyncModuleSyncModuleConfigurationPair)getItem(position);
    SyncModuleConfiguration syncModuleConfiguration = pair.getSyncModuleConfiguration();

    TextView txtvwSyncModuleName = (TextView)convertView.findViewById(R.id.txtvwSyncModuleName);
    txtvwSyncModuleName.setText(pair.getSyncModule().getName());

    Switch swtchEnableSyncModule = (Switch)convertView.findViewById(R.id.swtchEnableSyncModule);
    swtchEnableSyncModule.setTag(pair);
    swtchEnableSyncModule.setChecked(pair.isEnabled());
    swtchEnableSyncModule.setOnCheckedChangeListener(swtchEnableSyncModuleCheckedChangeListener);

    CheckBox chkbxBidirectional = (CheckBox)convertView.findViewById(R.id.chkbxBidirectional);
    chkbxBidirectional.setTag(pair);
    chkbxBidirectional.setChecked(pair.isBidirectional());
    chkbxBidirectional.setOnCheckedChangeListener(chkbxBidirectionalCheckedChangeListener);

    setControlsEnabledState(pair, txtvwSyncModuleName, chkbxBidirectional);

    return convertView;
  }


  protected void setControlsEnabledState(SyncModuleSyncModuleConfigurationPair pair, ViewGroup parentLayout) {
    TextView txtvwSyncModuleName = (TextView)parentLayout.findViewById(R.id.txtvwSyncModuleName);
    CheckBox chkbxBidirectional = (CheckBox)parentLayout.findViewById(R.id.chkbxBidirectional);

    setControlsEnabledState(pair, txtvwSyncModuleName, chkbxBidirectional);
  }

  protected void setControlsEnabledState(SyncModuleSyncModuleConfigurationPair pair, TextView txtvwSyncModuleName, CheckBox chkbxBidirectional) {
    txtvwSyncModuleName.setEnabled(pair.isEnabled());
    chkbxBidirectional.setEnabled(pair.isEnabled());
  }

  protected void showIfSyncModuleConfigurationChanged(SyncModuleSyncModuleConfigurationPair pair, ViewGroup parentLayout) {
    TextView txtvwSyncModuleConfigurationChanged = (TextView)parentLayout.findViewById(R.id.txtvwSyncModuleConfigurationChanged);
    txtvwSyncModuleConfigurationChanged.setVisibility(pair.didConfigurationChange() ? View.VISIBLE : View.INVISIBLE);
  }


  protected CompoundButton.OnCheckedChangeListener swtchEnableSyncModuleCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      SyncModuleSyncModuleConfigurationPair pair = (SyncModuleSyncModuleConfigurationPair)buttonView.getTag();
      pair.setEnabled(isChecked);

      ViewGroup parentLayout = (ViewGroup)buttonView.getParent();

      setControlsEnabledState(pair, parentLayout);

      showIfSyncModuleConfigurationChanged(pair, parentLayout);
    }
  };

  protected CompoundButton.OnCheckedChangeListener chkbxBidirectionalCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      SyncModuleSyncModuleConfigurationPair pair = (SyncModuleSyncModuleConfigurationPair)buttonView.getTag();
      pair.setBidirectional(isChecked);

      ViewGroup parentLayout = (ViewGroup)buttonView.getParent().getParent();

      showIfSyncModuleConfigurationChanged(pair, parentLayout);
    }
  };

}
