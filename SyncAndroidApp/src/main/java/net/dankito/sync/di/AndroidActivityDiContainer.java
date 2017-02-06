package net.dankito.sync.di;

import android.app.Activity;

import net.dankito.android.util.AndroidOnUiThreadRunner;
import net.dankito.android.util.services.IPermissionsManager;
import net.dankito.android.util.services.PermissionsManager;
import net.dankito.utils.IOnUiThreadRunner;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidActivityDiContainer {

  protected final Activity activity;


  public AndroidActivityDiContainer(Activity activity) {
    this.activity = activity;
  }


  @Provides //scope is not necessary for parameters stored within the module
  public Activity getActivity() {
    return activity;
  }


  @Provides
  @Singleton
  public IOnUiThreadRunner provideOnUiThreadRunner() {
    return new AndroidOnUiThreadRunner(getActivity());
  }

  @Provides
  @Singleton
  public IPermissionsManager providePermissionsManager() {
    return new PermissionsManager(getActivity());
  }

}
