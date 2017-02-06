package net.dankito.sync.service;

import android.os.Binder;


public class SyncBackgroundServiceBinder extends Binder {

  protected SyncBackgroundService service;


  public SyncBackgroundServiceBinder(SyncBackgroundService service) {
    this.service = service;
  }


  public SyncBackgroundService getService() {
    return service;
  }

}
