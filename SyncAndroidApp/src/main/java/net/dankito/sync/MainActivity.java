package net.dankito.sync;

import android.os.Bundle;
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

import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.sync.adapter.KnownSynchronizedDiscoveredDevicesAdapter;
import net.dankito.sync.adapter.UnknownDiscoveredDevicesAdapter;
import net.dankito.sync.devices.DiscoveredDevice;
import net.dankito.sync.devices.DiscoveredDeviceType;
import net.dankito.sync.devices.DiscoveredDevicesListener;
import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.di.AndroidDiComponent;
import net.dankito.sync.di.AndroidDiContainer;
import net.dankito.sync.di.DaggerAndroidDiComponent;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


  protected static AndroidDiComponent component;

  public static AndroidDiComponent getComponent() {
    return component;
  }


  @Inject
  protected IDevicesManager devicesManager;

  @Inject
  protected IPermissionsManager permissionsManager;


  protected LinearLayout linlytUnknownDiscoveredDevices;

  protected LinearLayout linlytKnownSynchronizedDiscoveredDevices;

  protected TextView txtvwStartSyncAppOnOtherDeviceHint;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupDependencyInjection();

    setupUi();

    setupLogic();
  }

  protected void setupDependencyInjection() {
    component = DaggerAndroidDiComponent.builder()
        .androidDiContainer(new AndroidDiContainer(this))
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

    ListView lstvwUnknownDiscoveredDevices = (ListView)findViewById(R.id.lstvwUnknownDiscoveredDevices);
    lstvwUnknownDiscoveredDevices.setAdapter(new UnknownDiscoveredDevicesAdapter(this, devicesManager));

    ListView lstvwKnownSynchronizedDiscoveredDevices = (ListView)findViewById(R.id.lstvwKnownSynchronizedDiscoveredDevices);
    lstvwKnownSynchronizedDiscoveredDevices.setAdapter(new KnownSynchronizedDiscoveredDevicesAdapter(this, devicesManager));

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


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
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
    devicesManager.addDiscoveredDevicesListener(discoveredDevicesListener);

    devicesManager.start();
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
