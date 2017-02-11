package net.dankito.sync.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.MultiplePermissionsRequestCallback;
import net.dankito.sync.MainActivity;
import net.dankito.sync.R;
import net.dankito.sync.SyncModuleConfiguration;
import net.dankito.sync.adapter.SyncModuleConfigurationsAdapter;
import net.dankito.sync.communication.IClientCommunicator;
import net.dankito.sync.communication.callback.SendRequestCallback;
import net.dankito.sync.communication.message.RequestPermitSynchronizationResponseBody;
import net.dankito.sync.communication.message.RequestPermitSynchronizationResult;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeResponseBody;
import net.dankito.sync.communication.message.RespondToSynchronizationPermittingChallengeResult;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.synchronization.ISyncConfigurationManager;
import net.dankito.sync.synchronization.modules.AndroidSyncModuleBase;
import net.dankito.sync.synchronization.modules.ISyncModule;
import net.dankito.sync.synchronization.modules.ISyncModuleConfigurationManager;
import net.dankito.sync.synchronization.modules.SyncConfigurationChanges;
import net.dankito.sync.synchronization.modules.SyncConfigurationWithDevice;
import net.dankito.sync.synchronization.modules.SyncModuleSyncModuleConfigurationPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class SynchronizationSettingsActivity extends AppCompatActivity {

  public final static String REMOTE_DEVICE_UNIQUE_ID_EXTRA_NAME = "Remote_Device";


  @Inject
  protected ISyncModuleConfigurationManager syncModuleConfigurationManager;

  @Inject
  protected ISyncConfigurationManager syncConfigurationManager;

  @Inject
  protected IDevicesManager devicesManager;

  @Inject
  protected IPermissionsManager permissionsManager;

  @Inject
  protected IClientCommunicator clientCommunicator;


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

      this.syncModuleConfigurationsForDevice = syncModuleConfigurationManager.getSyncModuleConfigurationsForDevice(remoteDevice);
      lstvwSynModuleConfigurations.setAdapter(new SyncModuleConfigurationsAdapter(this, syncModuleConfigurationManager, syncModuleConfigurationsForDevice));

      btnStartSynchronizingWithDevice.setEnabled(true);
      btnStartSynchronizingWithDevice.setOnClickListener(btnStartSynchronizingWithDeviceClickListener);

      if(syncModuleConfigurationsForDevice.isSyncConfigurationPersisted()) {
        btnStartSynchronizingWithDevice.setText(R.string.update_synchronization_settings);
      }
    }
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == android.R.id.home) {
      closeActivity();
    }

    return super.onOptionsItemSelected(item);
  }


  protected void closeActivity() {
    finish();
  }


  protected void askDeviceIfSynchronizingIsPermitted(final DiscoveredDevice remoteDevice) {
    clientCommunicator.requestPermitSynchronization(remoteDevice, new SendRequestCallback<RequestPermitSynchronizationResponseBody>() {
      @Override
      public void done(Response<RequestPermitSynchronizationResponseBody> response) {
        if(response.isCouldHandleMessage()) {
          RequestPermitSynchronizationResponseBody responseBody = response.getBody();
          if(responseBody.getResult() == RequestPermitSynchronizationResult.RESPOND_TO_CHALLENGE) {
            getChallengeResponseFromUser(remoteDevice, responseBody.getNonce(), false);
          }
          else {
            showAlertSynchronizingIsNotPermitted(remoteDevice);
          }
        }
        else {
          showErrorMessage(response);
        }
      }
    });
  }

  protected void getChallengeResponseFromUser(final DiscoveredDevice remoteDevice, final String nonce, final boolean wasCodePreviouslyWronglyEntered) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        getChallengeResponseFromUserOnUiThread(remoteDevice, nonce, wasCodePreviouslyWronglyEntered);
      }
    });
  }

  protected void getChallengeResponseFromUserOnUiThread(final DiscoveredDevice remoteDevice, final String nonce, boolean wasCodePreviouslyWronglyEntered) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    String title = getString(R.string.alert_title_enter_response_code_for_permitting_synchronization, remoteDevice.getDevice().getDeviceFullDisplayName());
    if(wasCodePreviouslyWronglyEntered) {
      title = getString(R.string.alert_title_entered_response_code_was_wrong, remoteDevice.getDevice().getDeviceFullDisplayName());
    }
    builder.setTitle(title);

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    builder.setView(input);

    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String enteredResponse = input.getText().toString();
        sendChallengeResponseToRemote(remoteDevice, nonce, enteredResponse);

      }
    });
    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.show();
  }

  protected void sendChallengeResponseToRemote(final DiscoveredDevice remoteDevice, final String nonce, String enteredResponse) {
    clientCommunicator.respondToSynchronizationPermittingChallenge(remoteDevice, nonce, enteredResponse, new SendRequestCallback<RespondToSynchronizationPermittingChallengeResponseBody>() {
      @Override
      public void done(Response<RespondToSynchronizationPermittingChallengeResponseBody> response) {
        handleEnteredChallengeResponse(remoteDevice, response, nonce);
      }
    });
  }

  protected void handleEnteredChallengeResponse(DiscoveredDevice remoteDevice, Response<RespondToSynchronizationPermittingChallengeResponseBody> response, String nonce) {
    if(response.isCouldHandleMessage()) {
      RespondToSynchronizationPermittingChallengeResult result = response.getBody().getResult();
      if(result == RespondToSynchronizationPermittingChallengeResult.ALLOWED) {
        remoteAllowedSynchronization(remoteDevice);
      }
      else if(result == RespondToSynchronizationPermittingChallengeResult.WRONG_CODE) {
        getChallengeResponseFromUser(remoteDevice, nonce, true);
      }
      else {
        showAlertSynchronizingIsNotPermitted(remoteDevice);
      }
    }
    else {
      showErrorMessage(response);
    }
  }

  protected void remoteAllowedSynchronization(DiscoveredDevice remoteDevice) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        remoteAllowedSynchronizationOnUiThread();
      }
    });
  }

  protected void remoteAllowedSynchronizationOnUiThread() {
    startSynchronizingWithDevice();

    closeActivity();
  }

  protected void showAlertSynchronizingIsNotPermitted(DiscoveredDevice remoteDevice) {
    // TODO
  }

  protected void showErrorMessage(Response response) {
    // TODO
  }

  protected void startSynchronizingWithDevice() {
    final List<SyncModuleConfiguration> syncModuleConfigurations = new ArrayList<>();

    for(SyncModuleSyncModuleConfigurationPair pair : syncModuleConfigurationsForDevice.getSyncModuleConfigurations()) {
      SyncModuleConfiguration syncModuleConfiguration = pair.getSyncModuleConfiguration();
      syncModuleConfiguration.setEnabled(pair.isEnabled());
      syncModuleConfiguration.setBidirectional(pair.isBidirectional());
      syncModuleConfigurations.add(syncModuleConfiguration);
    }

    checkPermissions(syncModuleConfigurations, new MultiplePermissionsRequestCallback() {
      @Override
      public void permissionsCheckDone(Map<String, Boolean> checkPermissionsResult) {
        devicesManager.startSynchronizingWithDevice(remoteDevice, syncModuleConfigurations);
      }
    });
  }

  protected void updateSyncConfiguration() {
    final SyncConfigurationChanges changes = syncModuleConfigurationManager.updateSyncConfiguration(syncModuleConfigurationsForDevice);

    checkPermissions(syncModuleConfigurationsForDevice.getSyncConfiguration().getSyncModuleConfigurations(), new MultiplePermissionsRequestCallback() {
      @Override
      public void permissionsCheckDone(Map<String, Boolean> checkPermissionsResult) {
        syncConfigurationManager.syncConfigurationHasBeenUpdated(syncModuleConfigurationsForDevice.getSyncConfiguration(), changes);
      }
    });
  }


  protected void checkPermissions(List<SyncModuleConfiguration> syncModuleConfigurations, MultiplePermissionsRequestCallback callback) {
    List<String> permissions = new ArrayList<>(syncModuleConfigurations.size());
    List<String> rationalsToShow = new ArrayList<>(syncModuleConfigurations.size());

    for(int i = 0; i < syncModuleConfigurations.size(); i++) {
      SyncModuleConfiguration syncModuleConfiguration = syncModuleConfigurations.get(i);
      ISyncModule syncModule = syncConfigurationManager.getSyncModuleForSyncModuleConfiguration(syncModuleConfiguration);

      if(syncModule instanceof AndroidSyncModuleBase && syncModuleConfiguration.isEnabled()) {
        AndroidSyncModuleBase androidSyncModule = (AndroidSyncModuleBase)syncModule;
        permissions.add(androidSyncModule.getPermissionToReadEntities());
        rationalsToShow.add(getString(androidSyncModule.getPermissionRationaleResourceId()));
      }
    }

    permissionsManager.checkPermissions(permissions.toArray(new String[permissions.size()]), rationalsToShow.toArray(new String[rationalsToShow.size()]), callback);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  protected View.OnClickListener btnStartSynchronizingWithDeviceClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(syncModuleConfigurationsForDevice.isSyncConfigurationPersisted() == false) {
        askDeviceIfSynchronizingIsPermitted(remoteDevice);
      }
      else {
        updateSyncConfiguration();

        closeActivity();
      }
    }
  };

}
