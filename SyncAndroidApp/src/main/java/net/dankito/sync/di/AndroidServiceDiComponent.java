package net.dankito.sync.di;

import net.dankito.sync.service.SyncBackgroundService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ganymed on 03/11/16.
 */
@Singleton
@Component(modules = { AndroidServiceDiContainer.class } )
public interface AndroidServiceDiComponent {

  void inject(SyncBackgroundService service);

}
