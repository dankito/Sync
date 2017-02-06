package net.dankito.sync.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import net.dankito.sync.devices.IDevicesManager;
import net.dankito.sync.di.AndroidServiceDiComponent;
import net.dankito.sync.di.AndroidServiceDiContainer;
import net.dankito.sync.di.DaggerAndroidServiceDiComponent;
import net.dankito.sync.synchronization.ISyncConfigurationManager;

import javax.inject.Inject;


public class SyncBackgroundService extends Service {

  protected AndroidServiceDiContainer diContainer;

  protected AndroidServiceDiComponent component;

  protected IBinder binder;


  @Inject
  protected IDevicesManager devicesManager;

  @Inject
  protected ISyncConfigurationManager syncConfigurationManager;



  public SyncBackgroundService() {

  }

  @Override
  public void onCreate() {
    super.onCreate();

    if(binder == null) {
      binder = new SyncBackgroundServiceBinder(this);

      setupDependencyInjection();

      setupLogic();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return Service.START_STICKY;
  }


  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }


  protected void setupDependencyInjection() {
    diContainer = new AndroidServiceDiContainer(this);

    component = DaggerAndroidServiceDiComponent.builder()
        .androidServiceDiContainer(diContainer)
        .build();

    component.inject(this);
  }


  protected void setupLogic() {
    devicesManager.start();
  }


  public AndroidServiceDiContainer getDiContainer() {
    return diContainer;
  }

  public AndroidServiceDiComponent getComponent() {
    return component;
  }

}
