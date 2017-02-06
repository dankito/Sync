package net.dankito.sync.di;

import net.dankito.sync.MainActivity;
import net.dankito.sync.activities.SynchronizationSettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ganymed on 03/11/16.
 */
@Singleton
@Component(modules = { AndroidDiContainer.class, AndroidServiceDiContainer.class } )
public interface AndroidDiComponent {

  // to update the fields in your activities
  void inject(MainActivity activity);

  void inject(SynchronizationSettingsActivity activity);

}
