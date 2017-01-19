package net.dankito.sync.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import net.dankito.sync.MainActivity;
import net.dankito.sync.R;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.adapter.SyncModuleConfigurationsAdapter;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncConfigurationWithDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SynchronizationSettingsActivity extends AppCompatActivity {

  public final static String REMOTE_DEVICE_UNIQUE_ID_EXTRA_NAME = "Remote_Device";


  @Inject
  protected ISyncModuleConfigurationManager syncModuleConfigurationManager;

  @Inject
  protected IDevicesManager devicesManager;


  protected DiscoveredDevice remoteDevice;

  protected SyncConfigurationWithDevice syncModuleConfigurationsForDevice;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupDependencyInjection();

    Intent intent = getIntent();
    if(intent != null) {
      String remoteUniqueDeviceId = intent.getStringExtra(REMOTE_DEVICE_UNIQUE_ID_EXTRA_NAME);
      if(remoteUniqueDeviceId != null) {
        remoteDevice = devicesManager.getDiscoveredDeviceForId(remoteUniqueDeviceId);
      }
    }

    setupUI();
  }

  protected void setupDependencyInjection() {
    MainActivity.getComponent().inject(this);
  }

  protected void setupUI() {
    setContentView(R.layout.activity_synchronization_settings);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);

    ListView lstvwSynModuleConfigurations = (ListView)findViewById(R.id.lstvwSynModuleConfigurations);

    Button btnStartSynchronizingWithDevice = (Button)findViewById(R.id.btnStartSynchronizingWithDevice);

    if(remoteDevice == null) { // TODO: what to do in this case?
      // by default Activities title is then set to 'Error occurred'
    }
    else {
      setTitle(remoteDevice.getDevice().getDeviceFullDisplayName());

      btnStartSynchronizingWithDevice.setEnabled(true);
      btnStartSynchronizingWithDevice.setOnClickListener(btnStartSynchronizingWithDeviceClickListener);

      this.syncModuleConfigurationsForDevice = syncModuleConfigurationManager.getSyncModuleConfigurationsForDevice(remoteDevice);
      lstvwSynModuleConfigurations.setAdapter(new SyncModuleConfigurationsAdapter(this, syncModuleConfigurationManager, syncModuleConfigurationsForDevice));
    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == android.R.id.home) {
      finish();
    }

    return super.onOptionsItemSelected(item);
  }


  protected void startSynchronizingWithDevice() {
    List<SyncModuleConfiguration> syncModuleConfigurations = new ArrayList<>();

    for(SyncModuleSyncModuleConfigurationPair pair : syncModuleConfigurationsForDevice.getSyncModuleConfigurations()) {
      if(pair.isEnabled()) {
        SyncModuleConfiguration syncModuleConfiguration = pair.getSyncModuleConfiguration();
        syncModuleConfiguration.setBidirectional(pair.isBidirectional());
        syncModuleConfigurations.add(syncModuleConfiguration);
      }
    }

    devicesManager.startSynchronizingWithDevice(remoteDevice, syncModuleConfigurations);
  }


  protected View.OnClickListener btnStartSynchronizingWithDeviceClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(syncModuleConfigurationsForDevice.isSyncConfigurationPersisted() == false) {
        startSynchronizingWithDevice();
      }
    }
  };

}
