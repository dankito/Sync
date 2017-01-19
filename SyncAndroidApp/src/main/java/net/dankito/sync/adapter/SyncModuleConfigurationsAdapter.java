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
import net.dankito.sync.synchronization.modules.SyncConfigurationWithDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;


public class SyncModuleConfigurationsAdapter extends BaseAdapter {

  protected Activity activity;

  protected SyncConfigurationWithDevice syncModuleConfigurationsForDevice;


  public SyncModuleConfigurationsAdapter(Activity activity, SyncConfigurationWithDevice syncModuleConfigurationsForDevice) {
    this.activity = activity;
    this.syncModuleConfigurationsForDevice = syncModuleConfigurationsForDevice;
  }


  @Override
  public int getCount() {
    return syncModuleConfigurationsForDevice.getSyncModuleConfigurations().size();
  }

  @Override
  public Object getItem(int position) {
    return syncModuleConfigurationsForDevice.getSyncModuleConfigurations().get(position);
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
    txtvwSyncModuleName.setText(pair.getSyncModule().getSyncEntityTypesItCanHandle()[0]);

    Switch swtchEnableSyncModule = (Switch)convertView.findViewById(R.id.swtchEnableSyncModule);
    swtchEnableSyncModule.setChecked(pair.isEnabled());
    swtchEnableSyncModule.setTag(pair);
    swtchEnableSyncModule.setOnCheckedChangeListener(swtchEnableSyncModuleCheckedChangeListener);

    CheckBox chkbxBidirectional = (CheckBox)convertView.findViewById(R.id.chkbxBidirectional);
    chkbxBidirectional.setChecked(syncModuleConfiguration.isBiDirectional());

    txtvwSyncModuleName.setEnabled(pair.isEnabled());
    chkbxBidirectional.setEnabled(pair.isEnabled());

    return convertView;
  }


  protected CompoundButton.OnCheckedChangeListener swtchEnableSyncModuleCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      SyncModuleSyncModuleConfigurationPair pair = (SyncModuleSyncModuleConfigurationPair)buttonView.getTag();
      pair.setEnabled(isChecked);

      ViewGroup parentLayout = (ViewGroup)buttonView.getParent();

      TextView txtvwSyncModuleName = (TextView)parentLayout.findViewById(R.id.txtvwSyncModuleName);
      CheckBox chkbxBidirectional = (CheckBox)parentLayout.findViewById(R.id.chkbxBidirectional);

      txtvwSyncModuleName.setEnabled(pair.isEnabled());
      chkbxBidirectional.setEnabled(pair.isEnabled());
    }
  };

}
