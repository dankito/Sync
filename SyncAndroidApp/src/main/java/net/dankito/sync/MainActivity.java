package net.dankito.sync;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.dankito.android.util.AlertHelper;
import net.dankito.android.util.ShowAlertDialogCallback;
import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.adapter.KnownSynchronizedDiscoveredDevicesAdapter;
import net.dankito.sync.adapter.UnknownDiscoveredDevicesAdapter;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.di.AndroidActivityDiComponent;
import net.dankito.sync.di.AndroidActivityDiContainer;
import net.dankito.sync.di.DaggerAndroidActivityDiComponent;
import net.dankito.sync.service.SyncBackgroundService;
import net.dankito.sync.service.SyncBackgroundServiceBinder;
import net.dankito.sync.util.AndroidIsSynchronizationPermittedHandler;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


  protected static AndroidActivityDiComponent component;

  public static AndroidActivityDiComponent getComponent() {
    return component;
  }


  @Inject
  protected IDevicesManager devicesManager;

  @Inject
  protected IPermissionsManager permissionsManager;


  protected SyncBackgroundService syncBackgroundService = null;


  protected ListView lstvwUnknownDiscoveredDevices;

  protected ListView lstvwKnownSynchronizedDiscoveredDevices;

  protected LinearLayout linlytUnknownDiscoveredDevices;

  protected LinearLayout linlytKnownSynchronizedDiscoveredDevices;

  protected TextView txtvwStartSyncAppOnOtherDeviceHint;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupUi();

    if(getIntent() != null) {
      handleIntent(getIntent());
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    Intent intent= new Intent(this, SyncBackgroundService.class);
    startService(intent);
    bindService(intent, syncBackgroundServiceConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onPause() {
    super.onPause();

    unbindService(syncBackgroundServiceConnection);
  }

  protected void setupActivity() {
    if(isAlreadySetup() == false) {
      setupDependencyInjection();

      setupLogic();
    }
  }

  protected boolean isAlreadySetup() {
    return component != null;
  }

  protected void setupDependencyInjection() {
    component = DaggerAndroidActivityDiComponent.builder()
        .androidActivityDiContainer(new AndroidActivityDiContainer(this))
        .androidServiceDiContainer(syncBackgroundService.getDiContainer())
        .build();

    component.inject(this);
  }

  protected void setupUi() {
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.setDrawerIndicatorEnabled(false);
    toggle.syncState();

//    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//    navigationView.setNavigationItemSelectedListener(this);

    lstvwUnknownDiscoveredDevices = (ListView)findViewById(R.id.lstvwUnknownDiscoveredDevices);

    lstvwKnownSynchronizedDiscoveredDevices = (ListView)findViewById(R.id.lstvwKnownSynchronizedDiscoveredDevices);

    linlytUnknownDiscoveredDevices = (LinearLayout)findViewById(R.id.linlytUnknownDiscoveredDevices);

    linlytKnownSynchronizedDiscoveredDevices = (LinearLayout)findViewById(R.id.linlytKnownSynchronizedDiscoveredDevices);
    linlytKnownSynchronizedDiscoveredDevices.setVisibility(View.GONE);

    txtvwStartSyncAppOnOtherDeviceHint = (TextView)findViewById(R.id.txtvwStartSyncAppOnOtherDeviceHint);
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }


  protected void handleIntent(Intent intent) {
    if(intent.hasExtra(AndroidIsSynchronizationPermittedHandler.IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME)) {
      switch(intent.getStringExtra(AndroidIsSynchronizationPermittedHandler.IS_SYNCHRONIZATION_PERMITTED_HANDLER_EXTRA_NAME)) {
        case AndroidIsSynchronizationPermittedHandler.SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION:
          handleShouldPermitSynchronizingWithDeviceIntent(intent);
          break;
        case AndroidIsSynchronizationPermittedHandler.SHOW_CORRECT_RESPONSE_TO_USER_NON_BLOCKING_ACTION:
          handleShowCorrectResponseToUserIntent(intent);
          break;
      }
    }
  }

  protected void handleShouldPermitSynchronizingWithDeviceIntent(Intent intent) {
    String deviceInfoString = intent.getStringExtra(AndroidIsSynchronizationPermittedHandler.DEVICE_INFO_EXTRA_NAME);

    String message = getString(R.string.alert_message_permit_device_to_synchronize, deviceInfoString);
    String alertTitle = getString(R.string.alert_title_permit_device_to_synchronize);

    AlertHelper.showConfirmMessage(this, message, alertTitle, new ShowAlertDialogCallback() {
      @Override
      public void optionSelected(boolean hasConfirmed) {
        sendShouldPermitSynchronizingWithDeviceResult(hasConfirmed);
      }
    });
  }

  private void sendShouldPermitSynchronizingWithDeviceResult(boolean permitsSynchronization) {
    Intent sendResultIntent = new Intent(AndroidIsSynchronizationPermittedHandler.SHOULD_PERMIT_SYNCHRONIZING_WITH_DEVICE_ACTION);

    sendResultIntent.putExtra(AndroidIsSynchronizationPermittedHandler.PERMITS_SYNCHRONIZATION_EXTRA_NAME, permitsSynchronization);

    sendBroadcast(sendResultIntent);
  }

  protected void handleShowCorrectResponseToUserIntent(Intent intent) {
    String deviceInfoString = intent.getStringExtra(AndroidIsSynchronizationPermittedHandler.DEVICE_INFO_EXTRA_NAME);
    String correctResponse = intent.getStringExtra(AndroidIsSynchronizationPermittedHandler.CORRECT_RESPONSE_EXTRA_NAME);

    String message = getString(R.string.alert_message_enter_this_code_on_remote_device, deviceInfoString, correctResponse);

    AlertHelper.showMessage(this, message);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
//    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_camera) {
      // Handle the camera action
    } else if (id == R.id.nav_gallery) {

    } else if (id == R.id.nav_slideshow) {

    } else if (id == R.id.nav_manage) {

    } else if (id == R.id.nav_share) {

    } else if (id == R.id.nav_send) {

    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }


  protected void setupLogic() {
    lstvwUnknownDiscoveredDevices.setAdapter(new UnknownDiscoveredDevicesAdapter(this, devicesManager));
    lstvwKnownSynchronizedDiscoveredDevices.setAdapter(new KnownSynchronizedDiscoveredDevicesAdapter(this, devicesManager));

    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);
    discoveredDevicesChanged();
  }


  protected ServiceConnection syncBackgroundServiceConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      SyncBackgroundServiceBinder backgroundServiceBinder = (SyncBackgroundServiceBinder) binder;
      syncBackgroundService = backgroundServiceBinder.getService();

      setupActivity();
    }

    public void onServiceDisconnected(ComponentName className) {
      syncBackgroundService = null;
    }
  };


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
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        discoveredDevicesChanged();
      }
    });
  }

  protected void discoveredDevicesChanged() {
    int countDiscoveredDevices = devicesManager.getAllDiscoveredDevices().size();
    int countDiscoveredKnownSynchronizedDevices = devicesManager.getKnownSynchronizedDiscoveredDevices().size();

    txtvwStartSyncAppOnOtherDeviceHint.setVisibility(countDiscoveredDevices == 0 ? View.VISIBLE : View.GONE);

    linlytKnownSynchronizedDiscoveredDevices.setVisibility(countDiscoveredKnownSynchronizedDevices > 0 ? View.VISIBLE : View.GONE);
  }

}
